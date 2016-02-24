

/**
 * HumanTaskUploader.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.humantask.stub.upload;

    /*
     *  HumanTaskUploader java interface
     */

    public interface HumanTaskUploader {
          

        /**
          * Auto generated method signature
          * 
                    * @param uploadHumanTask1
                
         */

         
                     public void uploadHumanTask(

                        org.wso2.carbon.humantask.stub.upload.types.UploadedFileItem[] fileItems2)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param uploadHumanTask1
            
          */
        public void startuploadHumanTask(

            org.wso2.carbon.humantask.stub.upload.types.UploadedFileItem[] fileItems2,

            final org.wso2.carbon.humantask.stub.upload.HumanTaskUploaderCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    