package ai.fd.mimi.prism;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class ClientComCtrl {
    private String accessToken = "";
    private XMLUtil util = null;

    private String srChunkedRequestXmlData = "";
    private ArrayList<byte[]> srChunkedRequestBinaryData = null;

    private boolean transferEncodingChunked = false;

    public ClientComCtrl(String accessToken)  {
        this.accessToken = accessToken;
        util = new XMLUtil();
    }

    /**
     * 分割送信の表明
     *
     * @return
     */
    public boolean isTransferEncodingChunked() {
        return transferEncodingChunked;
    }

    public void setTransferEncodingChunked(boolean chunked) {
        transferEncodingChunked = chunked;
    }

    /**
     * 分割送信 終了表明
     *
     * @return
     */
    public ResponseData request() throws ClientComCtrlExcepiton, IOException, SAXException {
        if (transferEncodingChunked) {
            Recognizer recog = new Recognizer(accessToken);
            RequestData requestData = util.parseXML(srChunkedRequestXmlData);
            return recog.recognize(requestData, srChunkedRequestBinaryData, -1);
        } else {
            throw new IllegalStateException();
        }
    }


    /**
     * 分割送信 データ送信
     *
     * @param binaryDataList
     * @return
     */
    public ResponseData request(ArrayList<byte[]> binaryDataList) throws IOException {
        if(isTransferEncodingChunked()) {
            for (byte[] b : binaryDataList) {
                srChunkedRequestBinaryData.add(b);
            }
        } else {
            throw new IllegalStateException();
        }
        return null;
    }

    /**
     * 分割送信　データ送信
     *
     * @param binaryData
     * @return
     */
    public ResponseData request(byte[] binaryData) throws IOException {
        if(isTransferEncodingChunked()) {
            srChunkedRequestBinaryData.add(binaryData);
        } else {
            throw new IllegalStateException();
        }
        return null;
    }

    /**
     * SR 分割送信 開始表明
     * MT SS
     *
     * @param xmlData
     * @return
     */
    public ResponseData request(String xmlData) throws ClientComCtrlExcepiton, IOException, SAXException {
        RequestData request = util.parseXML(xmlData);
        switch (request.type) {
            case SR:
                if(isTransferEncodingChunked()) {
                    srChunkedRequestXmlData = xmlData;
                    srChunkedRequestBinaryData = new ArrayList<byte[]>();
                    return null;
                } else {
                    // SR一括送信用のメソッドではない
                    throw new IllegalStateException();
                }
            case MT:
                Translator translator = new Translator(accessToken);
                return translator.translate(request);
            case SS:
                SpeechSynthesizer synth = new SpeechSynthesizer(accessToken);
                return synth.synthesize(request);
            case NONE:
            default:
        }
        return null;
    }

    /**
     * 一括送信
     *
     * @param xmlData
     * @param binaryDataList
     * @return
     */
    public ResponseData request(String xmlData, ArrayList<byte[]> binaryDataList) throws ClientComCtrlExcepiton, IOException, SAXException {
        RequestData request = util.parseXML(xmlData);
        switch (request.type) {
            case SR:
                if (isTransferEncodingChunked()) {
                    // 分割SS用のメソッドではない
                    throw new IllegalStateException();
                } else {
                    Recognizer recog = new Recognizer(accessToken);
                    RequestData requestData = util.parseXML(xmlData);
                    return recog.recognize(requestData, binaryDataList, -1);
                }
            case MT:
                //MT用メソッドではない
                throw new IllegalStateException();
            case SS:
                //SS用メソッドではない
                throw new IllegalStateException();
            case NONE:
            default:
                throw new IllegalStateException();
        }
    }

    public ResponseData request(String xmlData, byte[] binaryData) throws ClientComCtrlExcepiton, IOException, SAXException {
        if (transferEncodingChunked) {
            ArrayList<byte[]> list = new ArrayList<byte[]>();
            list.add(binaryData);
            request(xmlData, list);
        } else {
            throw new IllegalStateException();
        }
        return null;
    }
}