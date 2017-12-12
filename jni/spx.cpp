#ifdef WIN32
#include <windows.h>
#include <stdio.h>
#include <stdlib.h>
#else if
#include <unistd.h>
#endif

#include <jni.h>
#include <string.h>
#include <speex/speex_echo.h>
#include <speex/speex_preprocess.h>
#include <speex/speex.h>


#define FRAME_SIZE 160

typedef struct SPXOBJ{
	SpeexBits ebits;
	SpeexBits dbits;
	void *enc_state;
	void *dec_state;
	int compression;
	const int enc_frame_size;
	const int dec_frame_size;
	SpeexPreprocessState* m_pPreprocessorState;
	SpeexEchoState* m_SpeexEchoState;

}SPXOBJ;

extern "C" JNIEXPORT void Java_com_ryong21_encode_Speex_EchoInit(JNIEnv *env, jobject obj,SPXOBJ* spx,jint filter_length, jint sampling_rate ,jboolean associatePreprocesser);

extern "C" JNIEXPORT SPXOBJ* JNICALL Java_com_ryong21_encode_Speex_open(JNIEnv *env, jobject obj,jint compression) 
{

	 

	SPXOBJ* spx=(SPXOBJ*)malloc(sizeof(SPXOBJ));
	const SpeexMode * smEnc=speex_lib_get_mode(SPEEX_MODEID_NB);
	const SpeexMode * smDec=speex_lib_get_mode(SPEEX_MODEID_NB);
 
	spx->enc_state = speex_encoder_init(smEnc);
	spx->dec_state = speex_decoder_init(smDec);
	spx->compression=compression;
	 
	speex_encoder_ctl(spx->enc_state, SPEEX_SET_QUALITY, &spx->compression);
	speex_encoder_ctl(spx->enc_state, SPEEX_GET_FRAME_SIZE, (void*)&spx->enc_frame_size);
	speex_decoder_ctl(spx->dec_state, SPEEX_GET_FRAME_SIZE, (void*)&spx->dec_frame_size);
	spx->m_pPreprocessorState=speex_preprocess_state_init(FRAME_SIZE, 8000);  

	speex_bits_init(&spx->ebits);
	speex_bits_init(&spx->dbits);

	Java_com_ryong21_encode_Speex_EchoInit(env,obj,spx,0,0,true);
	return spx;

}
  
extern "C" JNIEXPORT void JNICALL Java_com_ryong21_encode_Speex_Denoise(JNIEnv *env, jobject obj,SPXOBJ* spx,jint noiseSuppress) {
	 int denoise = 1;
	 speex_preprocess_ctl(spx->m_pPreprocessorState, SPEEX_PREPROCESS_SET_DENOISE, &denoise); //降噪  
	 speex_preprocess_ctl(spx->m_pPreprocessorState, SPEEX_PREPROCESS_SET_NOISE_SUPPRESS, &noiseSuppress); //设置噪声的dB  
}

extern "C" JNIEXPORT void JNICALL Java_com_ryong21_encode_Speex_AGC(JNIEnv *env, jobject obj,SPXOBJ* spx,jfloat level){//增益 
	int agc = 1;  
	speex_preprocess_ctl(spx->m_pPreprocessorState, SPEEX_PREPROCESS_SET_AGC, &agc);//增益   
	speex_preprocess_ctl(spx->m_pPreprocessorState, SPEEX_PREPROCESS_SET_AGC_LEVEL,&level);  
}

extern "C" JNIEXPORT void JNICALL Java_com_ryong21_encode_Speex_VAD(JNIEnv *env, jobject obj,SPXOBJ* spx,jint vadProbStart,jint vadProbContinue){//静音检测   
	 int vad = 1;  
    //int vadProbStart = 80;  
    //int vadProbContinue = 65;//有音量持续时间，0-100，越小持续越长  
    speex_preprocess_ctl(spx->m_pPreprocessorState, SPEEX_PREPROCESS_SET_VAD, &vad); //静音检测   
    speex_preprocess_ctl(spx->m_pPreprocessorState, SPEEX_PREPROCESS_SET_PROB_START , &vadProbStart); //Set probability required for the VAD to go from silence to voice    
    speex_preprocess_ctl(spx->m_pPreprocessorState, SPEEX_PREPROCESS_SET_PROB_CONTINUE, &vadProbContinue); //Set probability required for the VAD to stay in the voice state (integer percent)    
}


extern "C" JNIEXPORT jint JNICALL Java_com_ryong21_encode_Speex_encode(JNIEnv *env, jobject obj,SPXOBJ* spx, jshortArray lin, jint offset, jbyteArray encoded, jint size) {
	const int enc_frame_size=160;
	jshort buffer[enc_frame_size];
	jbyte output_buffer[enc_frame_size];
	int nsamples = (size-1)/enc_frame_size + 1;
	int i, tot_bytes = 0;


	//if (!codec_open)
	//	return 0;

	for (i = 0; i < nsamples; i++) {
		env->GetShortArrayRegion(lin, offset + i*enc_frame_size, enc_frame_size, buffer);
		speex_bits_reset(&spx->ebits);
		if(spx->m_pPreprocessorState==NULL)
		{
			speex_encode_int(spx->enc_state, buffer, &spx->ebits);
		}
		else
		{
			//检测是否静音
			if (speex_preprocess_run(spx->m_pPreprocessorState, (spx_int16_t *)buffer)) 
			{ 
				speex_encode_int(spx->enc_state, buffer, &spx->ebits);
			}
			else
			{
				//没有声音
			}
		}
		
	}
	//env->GetShortArrayRegion(lin, offset, enc_frame_size, buffer);
	//speex_encode_int(enc_state, buffer, &ebits);

	tot_bytes = speex_bits_write(&spx->ebits, (char *)output_buffer,enc_frame_size);
	env->SetByteArrayRegion(encoded, 0, tot_bytes, output_buffer);

    return (jint)tot_bytes;
}


extern "C" JNIEXPORT jint JNICALL Java_com_ryong21_encode_Speex_decode (JNIEnv *env, jobject obj,SPXOBJ* spx, jbyteArray encoded, jshortArray lin, jint size) 
{
	const int dec_frame_size=160;
    jbyte buffer[dec_frame_size];
    jshort output_buffer[dec_frame_size];
    jsize encoded_length = size;

	//if (!codec_open)
	//	return 0;

	env->GetByteArrayRegion(encoded, 0, encoded_length, buffer);
	speex_bits_read_from(&spx->dbits, (char *)buffer, encoded_length);

	speex_decode_int(spx->dec_state, &spx->dbits, output_buffer);
	env->SetShortArrayRegion(lin, 0, dec_frame_size, output_buffer);

	return (jint)dec_frame_size;
}



extern "C" JNIEXPORT void Java_com_ryong21_encode_Speex_EchoInit(JNIEnv *env, jobject obj,SPXOBJ* spx,jint filter_length, jint sampling_rate ,jboolean associatePreprocesser)
{
    //SpeexEchoReset(); 
	int m_nFilterLen  = 160*8;
    int m_nSampleRate = 8000;

    spx->m_SpeexEchoState = speex_echo_state_init(FRAME_SIZE, m_nFilterLen);
	if( spx->m_pPreprocessorState==NULL)
		  spx->m_pPreprocessorState = speex_preprocess_state_init(FRAME_SIZE, m_nSampleRate);

	speex_echo_ctl( spx->m_SpeexEchoState, SPEEX_ECHO_SET_SAMPLING_RATE, &m_nSampleRate);
	
    if(associatePreprocesser)
    {
        speex_preprocess_ctl(spx->m_pPreprocessorState, SPEEX_PREPROCESS_SET_ECHO_STATE,spx->m_SpeexEchoState);
    }

   /* m_pfNoise = new float[FRAME_SIZE+1];
    m_bSpeexEchoHasInit = true;*/
	

}

extern "C" JNIEXPORT void Java_com_ryong21_encode_Speex_EchoCancellation(JNIEnv *env, jobject obj,SPXOBJ* spx,jshortArray play,jshortArray mic,jshortArray output)
{
	const int frame_size=160;
	jshort play_buffer[frame_size];
	jshort mic_buffer[frame_size];
	jshort out_buffer[frame_size];

	env->GetShortArrayRegion(play, 0, frame_size, play_buffer);
	env->GetShortArrayRegion(mic, 0, frame_size, mic_buffer);

	speex_echo_cancellation(spx->m_SpeexEchoState, play_buffer, mic_buffer, out_buffer);
	speex_preprocess_run(spx->m_pPreprocessorState, out_buffer); 

	env->SetShortArrayRegion(output, 0, frame_size, out_buffer);
}


extern "C" JNIEXPORT void Java_com_ryong21_encode_Speex_EchoCapture(JNIEnv *env, jobject obj,SPXOBJ* spx,jshortArray input_frame, jshortArray output_frame)
{
	const int frame_size=160;
	jshort input_buffer[frame_size];
	jshort output_buffer[frame_size];
	env->GetShortArrayRegion(input_frame, 0, frame_size, input_buffer);
    speex_echo_capture(spx->m_SpeexEchoState, input_buffer, output_buffer);
	speex_preprocess_run(spx->m_pPreprocessorState, output_buffer);
	env->SetShortArrayRegion(output_frame, 0, frame_size, output_buffer);
}
extern "C" JNIEXPORT void Java_com_ryong21_encode_Speex_EchoPlayback(JNIEnv *env, jobject obj,SPXOBJ* spx,jshortArray echo_frame)
{
	const int frame_size=160;
	jshort echo_buffer[frame_size];
	env->GetShortArrayRegion(echo_frame, 0, frame_size, echo_buffer);
    speex_echo_playback(spx->m_SpeexEchoState, echo_buffer);
}

extern "C" JNIEXPORT void JNICALL Java_com_ryong21_encode_Speex_close(JNIEnv *env, jobject obj,SPXOBJ* spx) 
{
	if (spx->m_pPreprocessorState != NULL)
    {
        speex_preprocess_state_destroy(spx->m_pPreprocessorState);
        spx->m_pPreprocessorState = NULL;
    }

	speex_bits_destroy(&spx->ebits);
	speex_bits_destroy(&spx->dbits);
	speex_decoder_destroy(spx->dec_state);
	speex_encoder_destroy(spx->enc_state);
}

