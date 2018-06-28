${TOTALTEST_JOBCARD}
//***   SPECIFY JOBCARD IN TOTALTEST PREFERENCES TO SUBSTITUTE
//*
//*** THE JOB CARD MUST INCLUDE A NOTIFY STATEMENT SUCH 
//*** AS NOTIFY=&SYSUID and also a REGION=0M parameter
//*
//********************************************************************
//* Execute Target Runner under TSO using the DSN command
//*    and the RUN subcommand
//********************************************************************
//RUNBD2 EXEC PGM=IKJEFT01,REGION=0M
//*
//* You need to modify the following DD statements.
//*
//* The first DD statement should be changed to the ECC SLCXLOAD 
//* dataset that contains the Topaz for Total Test TTTRUNNR program.
//*
//* The second DD statement should be changed to the loadlib
//* containing the programs to run during the test.
//*
//* The third DD statement should be changed to the loadlib
//* containing the TSO DSN command.
//*
//* The fourth DD statement is only required if running the JCL 
//* from Topaz for Total Test CLI with Code Coverage support.
//* If testing an LE application it should be changed to the
//* loadlib containing the COBOL runtime(CEE.SCEERUN), otherwise 
//* it can be removed.
//*
//STEPLIB DD DISP=SHR,DSN=CPWR.MLCXnnn.SLCXLOAD
//*  where MLCXnnn is MLCX170 OR HIGHER
//        DD DISP=SHR,DSN=<ADD LOADLIB CONTAINING COBOL TEST PROGRAM>
//        DD DISP=SHR,DSN=<LOADLIB CONTAINING 'DSN' COMMAND>
//*       DD DISP=SHR,DSN=<COBOL RUNTIME LOADLIB>
//*
//TRPARM DD *
*
*        Optionally set your custom exit program here:
* 
EXIT(NONE)
*
REPEAT(${TOTALTEST_REPEAT}),STUBS(${TOTALTEST_STUBS}),
DEBUG(OFF)
//*-----
//BININP DD DSN=${TOTALTEST_BININP},DISP=OLD
//BINREF DD DSN=${TOTALTEST_BINREF},DISP=OLD
//BINRES DD DSN=${TOTALTEST_BINRES},DISP=OLD
//*-----
//*-----
//*      Optional
//*      Add your custom DD statements
//SYSPRINT DD SYSOUT=*
//SYSOUT DD SYSOUT=*
//SYSTSPRT DD SYSOUT=*
//*
//* You need to modify the following RUN statement.
//*
//*
//* Change the <SUBSYSTEM ID> to your DB2 subsystem id(SSID).
//* Change the <PLAN NAME> to the plan name for your COBOL test program.
//*
//SYSTSIN  DD *
 DSN SYSTEM(<SUBSYSTEM ID>)
 RUN PROGRAM(TTTRUNNR) PLAN(<PLAN NAME>) PARMS('/NOSTAE')
 END
/*
//
