const domain = "101.55.28.135";
const wsUri = `ws://${domain}:3333/app/hello2`;

const peerConnectionConfig = {
  iceServers: [
    {
      urls: "stun:stun.l.google.com:19302",
    },
  ],
};

let websocket;

function init() {
  websocket = new WebSocket(wsUri);
  websocket.onopen = function (evt) {
    onOpen(evt);
  };
  websocket.onclose = function (evt) {
    onClose(evt);
  };
  websocket.onmessage = function (evt) {
    onMessage(evt);
  };
  websocket.onerror = function (evt) {
    onError(evt);
  };
}

function onOpen(evt) {
  doSend({ command: "request_offer" });
}

function onClose(evt) {
  writeToScreen("DISCONNECTED");
}

function onMessage(evt) {
  let peerConnection;
  const message = JSON.parse(evt.data);

  if (message.error) {
    console.log(message.error);
    return;
  }

  if (message.command == "offer") {
    peerConnection = new RTCPeerConnection(peerConnectionConfig);
    peerConnection.setRemoteDescription(message.sdp).then(function () {
      peerConnection.createAnswer().then(function (desc) {
        console.log("create Host Answer: success");
        peerConnection.setLocalDescription(desc).then(function () {
          console.log("Local SDP", peerConnection.localDescription);
          doSend({
            id: message.id,
            peer_id: message.peer_id,
            command: "answer",
            sdp: peerConnection.localDescription,
          });
        });
      });
    });

    if (message.candidates) {
      const candidates = message.candidates;
      console.log("[Message candidates]", candidates);
      for (let i = 0; i < candidates.length; i++) {
        const basicCandidate = candidates[i];
        const copyCandidate = Object.assign({}, basicCandidate);
        const ipRegexp = new RegExp(
          "\\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b",
          "gi"
        );
        copyCandidate.candidate = copyCandidate.candidate.replace(
          copyCandidate.candidate.match(ipRegexp)[0],
          domain
        );

        peerConnection
          .addIceCandidate(basicCandidate)
          .then(() =>
            console.log(
              `addIceCandidate : success[${basicCandidate.candidate}]`
            )
          );
        peerConnection
          .addIceCandidate(copyCandidate)
          .then(() =>
            console.log(
              `addIceCandidate Copy : success[${copyCandidate.candidate}`
            )
          );
      }
    }

    peerConnection.addEventListener("icecandidate", function (e) {
      if (e.candidate) {
        console.log("Send Candidate To Server: " + JSON.stringify(e.candidate));
        doSend({
          id: message.id,
          peer_id: message.peer_id,
          command: "candidate",
          candidates: [e.candidate],
        });
      }
    });

    peerConnection.onconnectionstatechange = function (e) {
      //iceConnectionState
      console.log(
        "[on connection state change]",
        peerConnection.connectionState,
        e
      );
    };

    peerConnection.oniceconnectionstatechange = function (e) {
      console.log(
        "[on ice connection state change]",
        peerConnection.iceConnectionState,
        e
      );
    };

    peerConnection.ontrack = function (e) {
      console.log("stream received.");
      mainStream = e.streams[0];
      const video = document.getElementById("player");
      video.srcObject = mainStream;
    };
  }

  //   websocket.close();
}

function onError(evt) {
  writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}

function doSend(message) {
  websocket.send(JSON.stringify(message));
}

function writeToScreen(message) {
  var pre = document.createElement("p");
  pre.style.wordWrap = "break-word";
  pre.innerHTML = message;
  output.appendChild(pre);
}

window.addEventListener("load", init, false);

//setTimeout(function() {
//  const video = document.getElementById("player");
//  video.play();
//}, 2000);
