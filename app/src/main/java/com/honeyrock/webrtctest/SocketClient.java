package com.honeyrock.webrtctest;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SessionDescription;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SocketClient extends WebSocketClient {

    Context context;

    public SocketClient(URI serverUri, Draft draft) {
        super(serverUri, draft);
    }

    public SocketClient(URI serverURI, Context context) {
        super(serverURI);
        this.context = context;
    }

    public SocketClient(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        send("{ \"command\": \"request_offer\" }");
        System.out.println("opened connection");
        // if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
    }

    @Override
    public void onMessage(String message) {
        System.out.println("received: " + message);

        PeerConnection peerConnection;
        JsonParser parser = new JsonParser();
        JsonObject data = (JsonObject)parser.parse(message);
        String command = data.get("command").getAsString();
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());
        PeerConnection.RTCConfiguration rtcConfig =
                new PeerConnection.RTCConfiguration(iceServers);
        if (command.equals("offer")){
            PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
            options.networkIgnoreMask = 0;

            PeerConnectionFactory.initialize(
                    PeerConnectionFactory.InitializationOptions.builder(context)
                            //.setFieldTrials(fieldTrials)
                            .setEnableInternalTracer(true)
                            .createInitializationOptions());


            PeerConnectionFactory factory = PeerConnectionFactory.builder()
                    .setOptions(options)
 /*                   .setAudioDeviceModule(adm)
                    .setVideoEncoderFactory(encoderFactory)
                    .setVideoDecoderFactory(decoderFactory)*/
                    .createPeerConnectionFactory();
            PCObserver pcObserver = new PCObserver();
            peerConnection = factory.createPeerConnection(rtcConfig, pcObserver);
            String sdp = data.get("sdp").getAsJsonObject().get("sdp").getAsString();
            Log.i("sdp", sdp);

            SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.ANSWER,sdp);
            SdpObserver sdpObserver = new SdpObserver();
            peerConnection.setRemoteDescription(sdpObserver ,sessionDescription);
            Log.i("RemoteDescription", peerConnection.getRemoteDescription().description);
            peerConnection.createAnswer(sdpObserver,null);




        }

    }



    @Override
    public void onClose(int code, String reason, boolean remote) {
        // The codecodes are documented in class org.java_websocket.framing.CloseFrame
        System.out.println(
                "Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: "
                        + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
        // if the error is fatal then onClose will be called additionally
    }





}


class PCObserver implements PeerConnection.Observer {

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {

    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {

    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {

    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

    }

    @Override
    public void onAddStream(MediaStream mediaStream) {

    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {

    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {

    }

    @Override
    public void onRenegotiationNeeded() {

    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

    }
}

class SdpObserver implements org.webrtc.SdpObserver{

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {

        Log.i("onCreateSuccess","onCreateSuccess");
    }

    @Override
    public void onSetSuccess() {
        Log.i("onSetSuccess","onSetSuccess");
    }

    @Override
    public void onCreateFailure(String s) {
        Log.i("onCreateFailure",s);
    }

    @Override
    public void onSetFailure(String s) {
        Log.i("onSetFailure",s);
    }


}

/*
class SDPObserver implements org.webrtc.SdpObserver {
    private SessionDescription localDescription;

    @Override
    public void onCreateSuccess(final SessionDescription desc) {
        if (localDescription != null) {
            reportError("Multiple SDP create.");
            return;
        }
        String sdp = desc.description;
        if (preferIsac) {
            sdp = preferCodec(sdp, AUDIO_CODEC_ISAC, true);
        }
        if (isVideoCallEnabled()) {
            sdp = preferCodec(sdp, getSdpVideoCodecName(peerConnectionParameters), false);
        }
        final SessionDescription newDesc = new SessionDescription(desc.type, sdp);
        localDescription = newDesc;
        executor.execute(() -> {
            if (peerConnection != null && !isError) {
                Log.d(TAG, "Set local SDP from " + desc.type);
                peerConnection.setLocalDescription(sdpObserver, newDesc);
            }
        });
    }

    @Override
    public void onSetSuccess() {

    }

    @Override
    public void onCreateFailure(String s) {

    }

    @Override
    public void onSetFailure(String s) {

    }
}*/
