/*================================================================
*   Copyright (C) 2015 All rights reserved.
*   
*   文件名称：base64.h
*   创 建 者：Zhang Yuanhao
*   邮    箱：bluefoxah@gmail.com
*   创建日期：2015年01月28日
*   描    述：
*
#pragma once
================================================================*/
#ifndef __BASE64_H__
#define __BASE64_H__
#include<iostream>
#include <iterator>
#include <vector>
using namespace std;

string base64_decode(const string &ascdata);
string base64_encode(const string &bindata);

template<typename T>
std::string HexStr(const T itbegin, const T itend)
{
    std::string rv;
    static const char hexmap[16] = { '0', '1', '2', '3', '4', '5', '6', '7',
                                     '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    rv.reserve(std::distance(itbegin, itend) * 2);
    for (T it = itbegin; it < itend; ++it)
    {
        unsigned char val = (unsigned char)(*it);
        rv.push_back(hexmap[val >> 4]);
        rv.push_back(hexmap[val & 15]);
    }
    return rv;
}

template<typename T>
inline std::string HexStr(const T& vch)
{
    return HexStr(vch.begin(), vch.end());
}
std::vector<unsigned char> ParseStringHex(const std::string& str);

#endif

