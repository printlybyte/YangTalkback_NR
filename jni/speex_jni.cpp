#include <jni.h>

#include <string.h>
#include <unistd.h>
#include <speex/speex_echo.h>
#include <speex/speex_preprocess.h>
#include <speex/speex.h>

#ifdef WIN32
#include <windows.h>
#include <stdio.h>
#include <stdlib.h>
#else if
#include <unistd.h>
#endif


static int codec_open = 0;

static int dec_frame_size;
static int enc_frame_size;

static SpeexBits ebits, dbits;
void *enc_state;
void *dec_state;
//以下为静音检测------------

bool      m_bSpeexEchoHasInit;
SpeexEchoState*   m_SpeexEchoState;
SpeexPreprocessState* m_pPreprocessorState;
int      m_nFilterLen;
int      m_nSampleRate;
float*   m_pfNoise;
int		 tcount;

static JavaVM *gJavaVM;

extern "C"
JNIEXPORT jint JNICALL Java_com_ryong21_encode_Speex_open
  (JNIEnv *env, jobject obj, jint compression) {
	int tmp;

	if (codec_open++ != 0)
		return (jint)0;

	speex_bits_init(&ebits);
	speex_bits_init(&dbits);

	enc_state = speex_encoder_init(&speex_nb_mode);
	dec_state = speex_decoder_init(&speex_nb_mode);
	tmp = compression;
	speex_encoder_ctl(enc_state, SPEEX_SET_QUALITY, &tmp);
	speex_encoder_ctl(enc_state, SPEEX_GET_FRAME_SIZE, &enc_frame_size);
	speex_decoder_ctl(dec_state, SPEEX_GET_FRAME_SIZE, &dec_frame_size);

	return (jint)0;
}


extern "C"
JNIEXPORT void JNICALL Java_com_ryong21_encode_Speex_preprocess
  (JNIEnv *env, jobject obj) {
	  
    int ret;  
    int j=0;  
    SpeexPreprocessState * m_st;  
    SpeexEchoState *echo_state;   
    m_st=m_pPreprocessorState=speex_preprocess_state_init(dec_frame_size, 8000);  
//  echo_state = speex_echo_state_init(160, 8000);    
    int denoise = 1;  
    int noiseSuppress = -25;  
    speex_preprocess_ctl(m_st, SPEEX_PREPROCESS_SET_DENOISE, &denoise); //降噪   
    speex_preprocess_ctl(m_st, SPEEX_PREPROCESS_SET_NOISE_SUPPRESS, &noiseSuppress); //设置噪声的dB   
    int agc = 1;  
    float q=24000;  
    //actually default is 8000(0,32768),here make it louder for voice is not loudy enough by default. 8000   
    speex_preprocess_ctl(m_st, SPEEX_PREPROCESS_SET_AGC, &agc);//增益   
    speex_preprocess_ctl(m_st, SPEEX_PREPROCESS_SET_AGC_LEVEL,&q);  
    int vad = 1;  
    int vadProbStart = 80;  
    int vadProbContinue = 65;//有音量持续时间，0-100，越小持续越长  
    speex_preprocess_ctl(m_st, SPEEX_PREPROCESS_SET_VAD, &vad); //静音检测   
    speex_preprocess_ctl(m_st, SPEEX_PREPROCESS_SET_PROB_START , &vadProbStart); //Set probability required for the VAD to go from silence to voice    
    speex_preprocess_ctl(m_st, SPEEX_PREPROCESS_SET_PROB_CONTINUE, &vadProbContinue); //Set probability required for the VAD to stay in the voice state (integer percent)    

}

extern "C"
JNIEXPORT jint JNICALL Java_com_ryong21_encode_Speex_encode
    (JNIEnv *env, jobject obj, jshortArray lin, jint offset, jbyteArray encoded, jint size) {

        jshort buffer[enc_frame_size];
        jbyte output_buffer[enc_frame_size];
	int nsamples = (size-1)/enc_frame_size + 1;
	int i, tot_bytes = 0;

	if (!codec_open)
		return 0;

	for (i = 0; i < nsamples; i++) {
		env->GetShortArrayRegion(lin, offset + i*enc_frame_size, enc_frame_size, buffer);
		speex_bits_reset(&ebits);
		if(m_pPreprocessorState==NULL)
		{
			speex_encode_int(enc_state, buffer, &ebits);
		}
		else
		{
			if (speex_preprocess_run(m_pPreprocessorState, (spx_int16_t *)buffer))//预处理 打开了静音检测和降噪   
			{ 
				speex_encode_int(enc_state, buffer, &ebits);
			}
			else
			{

			}
		}
		
	}
	//env->GetShortArrayRegion(lin, offset, enc_frame_size, buffer);
	//speex_encode_int(enc_state, buffer, &ebits);

	tot_bytes = speex_bits_write(&ebits, (char *)output_buffer,enc_frame_size);
	env->SetByteArrayRegion(encoded, 0, tot_bytes,
				output_buffer);

    return (jint)tot_bytes;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_ryong21_encode_Speex_decode
    (JNIEnv *env, jobject obj, jbyteArray encoded, jshortArray lin, jint size) {

        jbyte buffer[dec_frame_size];
        jshort output_buffer[dec_frame_size];
        jsize encoded_length = size;

	if (!codec_open)
		return 0;

	env->GetByteArrayRegion(encoded, 0, encoded_length, buffer);
	speex_bits_read_from(&dbits, (char *)buffer, encoded_length);
	speex_decode_int(dec_state, &dbits, output_buffer);
	env->SetShortArrayRegion(lin, 0, dec_frame_size,
				 output_buffer);

	return (jint)dec_frame_size;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_ryong21_encode_Speex_getFrameSize
    (JNIEnv *env, jobject obj) {

	if (!codec_open)
		return 0;
	return (jint)enc_frame_size;

}

extern "C"
JNIEXPORT void JNICALL Java_com_ryong21_encode_Speex_close
    (JNIEnv *env, jobject obj) {

	if (--codec_open != 0)
		return;

	if (m_pPreprocessorState != NULL)
    {
        speex_preprocess_state_destroy(m_pPreprocessorState);
        m_pPreprocessorState = NULL;
    }

	speex_bits_destroy(&ebits);
	speex_bits_destroy(&dbits);
	speex_decoder_destroy(dec_state);
	speex_encoder_destroy(enc_state);


}
