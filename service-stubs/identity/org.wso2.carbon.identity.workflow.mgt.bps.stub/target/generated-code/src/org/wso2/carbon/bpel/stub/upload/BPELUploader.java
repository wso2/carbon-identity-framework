

/**
 * BPELUploader.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.bpel.stub.upload;

    /*
     *  BPELUploader java interface
     */

    public interface BPELUploader {
          

        /**
          * Auto generated method signature
          * 
                    * @param uploadService1
                
         */

         
                     public void uploadService(

                        org.wso2.carbon.bpel.stub.upload.types.UploadedFileItem[] fileItems2)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param uploadService1
            
          */
        public void startuploadService(

            org.wso2.carbon.bpel.stub.upload.types.UploadedFileItem[] fileItems2,

            final org.wso2.carbon.bpel.stub.upload.BPELUploaderCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    