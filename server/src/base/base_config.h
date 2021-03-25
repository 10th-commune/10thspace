#ifndef __BASE_CONFIG_H__
#define __BASE_CONFIG_H__

#include <string>

#define TENTH_SPACE_FOR_TEST 1

#ifdef TENTH_SPACE_FOR_TEST
//测试
static const char OSS_ENDPOINT[] = "oss-cn-shenzhen.aliyuncs.com";
static const char OSS_ENDPOINT_INTERNAL[] = "oss-cn-shenzhen-internal.aliyuncs.com";
static const char ACCESS_KEY_ID[] = "hfZkfkpDsk5PoW2B";
static const char ACCESS_KEY_SECRET[] = "zeZV5r87kGrPPfBkUpYGgtBArsMI1J";
static const char BUCKET_NAME[] = "tenth";
static const char BUCKET_NAME2[] = "maomaojiang";


#else
//正式
static const char OSS_ENDPOINT[] = "oss-cn-shenzhen.aliyuncs.com";
static const char OSS_ENDPOINT_INTERNAL[] = "oss-cn-shenzhen-internal.aliyuncs.com";
static const char ACCESS_KEY_ID[] = "hfZkfkpDsk5PoW2B";
static const char ACCESS_KEY_SECRET[] = "zeZV5r87kGrPPfBkUpYGgtBArsMI1J";
static const char BUCKET_NAME[] = "10thspace2";
static const char BUCKET_NAME2[] = "10thspace1";

#endif

static std::string TENTH_AES_KEY = "01534667291233560123456709123456"; // 32 bytes
static std::string TENTH_AES_IV  = "0123456789123456"; // 16 bytes

#endif
