/*================================================================
*     Copyright (c) 2014年 lanhu. All rights reserved.
*   
*   文件名称：FileAction.cpp
*   创 建 者：Zhang Yuanhao
*   邮    箱：bluefoxah@gmail.com
*   创建日期：2014年12月31日
*   描    述：
*
================================================================*/
#include "ServerAction.h"
#include "IM.Server.pb.h"
#include "../ProxyConn.h"
#include "../DBPool.h"
#include <ctime>


namespace DB_PROXY {

    void saveStudyTime(CImPdu* pPdu, uint32_t conn_uuid)
    {
        IM::Server::IMSaveStudyTimeReq msg;
        if(msg.ParseFromArray(pPdu->GetBodyData(), pPdu->GetBodyLength()))
        {
            string nUserId = msg.user_id();
            uint32_t nStart = msg.start();
            uint32_t nDuration = msg.duration();

            CDBManager* pDBManager = CDBManager::getInstance();
		    CDBConn* pDBConn = pDBManager->GetDBConn("teamtalk_master");
		    if (pDBConn)
		    {
		    	string strSql = "insert into StudyTimeRecord ( `user_id`, `start`, `duration`, `create_time`) values(?, ?, ?, now())";
				// 必须在释放连接前delete CPrepareStatement对象，否则有可能多个线程操作mysql对象，会crash
				CPrepareStatement* pStmt = new CPrepareStatement();
				if (pStmt->Init(pDBConn->GetMysql(), strSql))
				{
					pStmt->SetParam(0, nUserId);
					pStmt->SetParam(1, nStart);
					pStmt->SetParam(2, nDuration);
					pStmt->ExecuteUpdate();
					//comment_id = pStmt->GetInsertId();
				}
				delete pStmt;

				//没有使用，相关逻辑移到客户端完成。
				/*char buf[256];
				int buf_len = 255;
				struct tm tm = *localtime((time_t *)&nStart);
				//strftime(buf, 255, "%Y-%m-%d %H:%M:%S", &tm);
				strftime(buf, buf_len, "%Y-%m-%d", &tm);
				buf[buf_len - 1] = '\0';

				string strSql = "insert into StudyTime ( `user_id`, `duration`, `create_time`) values(?, ?, ?, now())";
				// 必须在释放连接前delete CPrepareStatement对象，否则有可能多个线程操作mysql对象，会crash
				CPrepareStatement* pStmt2 = new CPrepareStatement();
				if (pStmt->Init(pDBConn->GetMysql(), strSql))
				{
					pStmt2->SetParam(0, nUserId);
					pStmt2->SetParam(1, nStart);
					pStmt2->SetParam(2, nDuration);
					pStmt2->ExecuteUpdate();
					//comment_id = pStmt->GetInsertId();
				}
				delete pStmt2;*/

				pDBManager->RelDBConn(pDBConn);

		    }
            
            log("save study time userId=%s, start=%u, duration=%u", nUserId.c_str(), nStart, nDuration);
        }
        else
        {
            log("parse pb failed");
        }
    }
    
};
