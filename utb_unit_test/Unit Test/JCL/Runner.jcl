//HUKLEB0X JOB ('EUDD,INTL'),'BERGLUND',NOTIFY=&SYSUID,
//             MSGLEVEL=(1,1),MSGCLASS=X,CLASS=A,REGION=0M
//*** INSERT JOB CARD HERE *** 
//*
//*** THE JOB CARD MUST INCLUDE A NOTIFY STATEMENT SUCH 
//*** AS NOTIFY=&SYSUID and also a REGION=0M parameter
//*
//********************************************************************
//* Execute Target Runner
//********************************************************************
//RUNNER EXEC PGM=TTTRUNNR
//*
//* You need to modify the following DD statements.
//*
//* The first DD statement should be changed to the loadlib 
//* containing the Topaz for Total Test 'TTTRUNNR" program.
//*
//* The second DD statement should be changed to the loadlib
//* containing the programs to run during the test.
//*
//* The third DD statement is only required if running the JCL 
//* from Topaz for Total Test CLI with Code Coverage support.
//* If testing an LE application it should be changed to the
//* loadlib containing the COBOL runtime(CEE.SCEERUN), otherwise 
//* it can be removed.
/*
//STEPLIB DD DISP=SHR,DSN=SYS2.CW.&CWGACX..SLCXLOAD
//*  where MLCXnnn is MLCX170 OR HIGHER
//        DD DISP=SHR,DSN=HUKLEB0.DEMO.LOAD.PDSE
//        DD DISP=SHR,DSN=CEE.SCEERUN
//*
//TRPARM DD *
*
*        Optionally set your custom exit program here:
* 
EXIT(NONE)
*
REPEAT(${TOTALTEST_REPEAT}),STUBS(${TOTALTEST_STUBS}),
DEBUG(ON)
/* 
//BININP DD DSN=${TOTALTEST_BININP},DISP=OLD
//BINREF DD DSN=${TOTALTEST_BINREF},DISP=OLD
//BINRES DD DSN=${TOTALTEST_BINRES},DISP=OLD
//*
//*      Optionally add your custom DD statements
//*DD1 DD DSN=HLQ.CUSTOM.TEST.LOAD,DISP=SHR
//EMPFILE  DD  DISP=SHR,DSN=HUKLEB0.DEMO.CWXTDATA          
//RPTFILE  DD  SYSOUT=*                                    
//SYSPRINT DD SYSOUT=*
//*
