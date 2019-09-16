import SockJS from "sockjs-client";
import Stomp from "stomp-websocket";
import noty from "noty";
import $ from 'jquery';
import uuid from "uuid";
import 'bootstrap/dist/css/bootstrap.min.css';
import {ACCESS_TOKEN, CHAT_ROOM_ID, CHAT_ROOM_MESSAGES_URI, FORM_AUTH_URI, WEB_SOCKET_URI} from "./constants";

const serialize_form = form => JSON.stringify(
    Array.from(new FormData(form).entries())
        .reduce((m, [key, value]) => Object.assign(m, {[key]: value}), {})
);
const uuidv1 = uuid.v1;

$(document).ready(function () {
        var stompClient = null;
        var socket = null;
        var chatRoomId = CHAT_ROOM_ID;
        var authSend = $('#loginForm');
        var inputMessage = $("#message");
        var btnSend = $("#send");
        var btnDisconnect = $("#disconnect");
        var newMessages = $("#newMessages");
        var spanSendTo = $("#sendTo");
        var btnPublic = $("#public");

        function logout() {
            disableInputMessage();
            disconnect();
            sessionStorage.setItem(ACCESS_TOKEN, null);
        }

        function httpAuth(event) {
            event.preventDefault();
            logout();
            const json = serialize_form(this);
            console.log(json);
            $.ajax({
                type: 'POST',
                url: FORM_AUTH_URI,
                dataType: 'json',
                data: json,
                contentType: 'application/json',
                success: function (response) {
                    sessionStorage.setItem(ACCESS_TOKEN, response[ACCESS_TOKEN]);
                    console.log("JWT:" + sessionStorage.getItem(ACCESS_TOKEN));
                    httpGetOldMessages();

                }
            })
        }

        function getHttpAuthHeader() {
            return {'Authorization': 'Bearer ' + sessionStorage.getItem(ACCESS_TOKEN)};
        }

        function httpGetOldMessages() {
            $.ajax({
                type: 'GET',
                url: CHAT_ROOM_MESSAGES_URI,
                headers: getHttpAuthHeader(),
                dataType: 'json',
                contentType: 'application/json',
                success: function (response) {
                    oldMessages(response);
                    console.log(response + JSON.stringify(response));
                }
            })
        }

        function httpSendMessage(message) {
            let json = JSON.stringify({
                "id": message.id,
                "message": message.text
            });
            $.ajax({
                type: 'POST',
                url: CHAT_ROOM_MESSAGES_URI,
                headers: getHttpAuthHeader(),
                dataType: 'json',
                data: json,
                contentType: 'application/json',
                success: function (response) {
                    console.log(response + JSON.stringify(response));
                }
            })
        }

        function disconnect() {
            if (stompClient !== null) {
                if(stompClient)
                    stompClient.disconnect(function() {
                        socket.close();
                    });
            }
        }
        function connect() {
            disconnect();
            console.log("Server disconnected");
            socket = new SockJS(WEB_SOCKET_URI + '?access_token=' +  sessionStorage.getItem(ACCESS_TOKEN));
            stompClient = Stomp.over(socket);
            stompClient.connect({'chatRoomId': chatRoomId}, stompSuccess, stompFailure);
        }

        function stompSuccess(frame) {
            stompClient.subscribe('/topic/' + chatRoomId + '.public.messages', publicMessages);
            successMessage("Your WebSocket connection was successfuly established!");
            setTimeout(enableInputMessage(), 10000);
        }

        function stompFailure(error) {
            errorMessage("Lost connection to WebSocket! Reconnecting in 10 seconds");
            disableInputMessage();
            setTimeout(connect, 10000);
        }


        function oldMessages(response) {

            newMessages.empty();
            var instantMessages = response.content;//.reverse();
            $.each(instantMessages, function (index, instantMessage) {
                appendPublicMessage(instantMessage);
            });

            scrollDownMessagesPanel();
            connect();

        }

        function publicMessages(message) {
            var instantMessage = JSON.parse(message.body);
            appendPublicMessage(instantMessage);
            scrollDownMessagesPanel();
        }

        function appendPublicMessage(instantMessage) {
            newMessages
                .append("<p>" + instantMessage.author.name + ": " + instantMessage.message + "</p>")
        }


        function sendMessage() {
            var instantMessage;

            if (inputMessageIsEmpty()) {
                inputMessage.focus();
                return;
            }

            if (spanSendTo.text() == "public") {
                instantMessage = {
                    'id': uuidv1(),
                    'text': inputMessage.val()
                }
            } else {
                instantMessage = {
                    'id': uuidv1(),
                    'text': inputMessage.val(),
                    'toUser': spanSendTo.text()
                }
            }

            httpSendMessage(instantMessage);
            inputMessage.val("").focus();
        }

        function inputMessageIsEmpty() {
            return inputMessage.val() == "";
        }

        function checkEnter(e) {
            var key = e.which;
            if (key == 13) {
                btnSend.click();
                return false;
            }
        }

        function scrollDownMessagesPanel() {
            newMessages.animate({"scrollTop": newMessages[0].scrollHeight}, "fast");
        }

        function enableInputMessage() {
            inputMessage.prop("disabled", false);
        }

        function disableInputMessage() {
            inputMessage.prop("disabled", true);
        }

        function successMessage(msg) {
            noty({
                text: msg,
                layout: 'top',
                type: 'success',
                timeout: 5000
            });
        }

        function errorMessage(msg) {
            noty({
                text: msg,
                layout: 'top',
                type: 'error',
                timeout: 5000
            });
        }

        authSend.on('submit', httpAuth);
        inputMessage.on("keypress", checkEnter).focus();
        btnSend.on("click", sendMessage);
        btnDisconnect.on("click", disconnect);
        btnPublic.on("click", function () {
            spanSendTo.text("public");
            inputMessage.focus();
        });

        scrollDownMessagesPanel();
        disableInputMessage();
    }
);