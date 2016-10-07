package de.xikolo.managers;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import com.google.gson.JsonSyntaxException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.java_websocket.WebSocket;
import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import de.xikolo.GlobalApplication;
import de.xikolo.data.entities.WebSocketMessage;
import de.xikolo.data.parser.ApiParser;
import de.xikolo.model.UserModel;
import de.xikolo.model.events.Event;
import de.xikolo.model.events.LoginEvent;
import de.xikolo.model.events.LogoutEvent;
import de.xikolo.model.events.NetworkStateEvent;
import de.xikolo.util.Config;
import de.xikolo.util.NetworkUtil;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class WebSocketManager {

    public static final String TAG = WebSocketManager.class.getSimpleName();

    private WebSocketClient webSocketClient;

    private URI uri;

    public WebSocketManager(String uri) {
        try {
            this.uri = new URI(uri);

            EventBus.getDefault().register(this);
        } catch (URISyntaxException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * Init WebSocket connection if not already opened.
     */
    public void initConnection(String token) {
        if (webSocketClient == null || (!isConnected() && !isConnecting())) {
            Map<String, String> headers = new HashMap<>();
            headers.put(Config.HEADER_AUTHORIZATION, Config.HEADER_AUTHORIZATION_PREFIX + token);

            webSocketClient = new WebSocketClient(uri, new Draft_10(), headers, 0) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    Log.i(TAG, "WebSocket opened");
                    EventBus.getDefault().post(new WebSocketConnectedEvent());
                }

                @Override
                public void onMessage(String message) {
                    if (Config.DEBUG) {
//                        Log.d(TAG, "WebSocket received message: " + message);
                    }
                    try {
                        EventBus.getDefault().post(new WebSocketMessageEvent(message));
                    } catch (JsonSyntaxException e) {
                        Log.e(TAG, "Couldn't parse WebSocket message: " + e.getMessage());
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.i(TAG, "WebSocket closed");
                    EventBus.getDefault().post(new WebSocketClosedEvent());
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    EventBus.getDefault().post(new WebSocketClosedEvent());
                }
            };

            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, null, null);
                webSocketClient.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sc));
                webSocketClient.connect();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    public void closeConnection() {
        if (isConnected()) {
            webSocketClient.close();
        }
    }

    /**
     * Sends message through the WebSocket.
     * @param message the message to send, has to be a valid JSON string.
     * @return true, if message could be sent and false, if not.
     */
    public boolean send(String message) {
        if (isConnected()) {
            webSocketClient.send(message);
            return true;
        } else {
            return false;
        }
    }

    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.getReadyState() == WebSocket.READYSTATE.OPEN;
    }

    private boolean isConnecting() {
        return webSocketClient != null && webSocketClient.getReadyState() == WebSocket.READYSTATE.CONNECTING;
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNetworkEvent(NetworkStateEvent event) {
        if (event.isOnline() && UserModel.isLoggedIn(GlobalApplication.getInstance())) {
            initConnection(UserModel.getToken(GlobalApplication.getInstance()));
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onLoginEvent(LoginEvent event) {
        if (NetworkUtil.isOnline(GlobalApplication.getInstance())) {
            initConnection(UserModel.getToken(GlobalApplication.getInstance()));
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onLogoutEvent(LogoutEvent event) {
        closeConnection();
    }

    static class WebSocketConnectedEvent extends Event {}

    static class WebSocketClosedEvent extends Event {}

    static class WebSocketMessageEvent extends Event {

        private WebSocketMessage webSocketMessage;

        WebSocketMessageEvent(String message) {
            super();
            this.webSocketMessage = ApiParser.parse(message, WebSocketMessage.class);
        }

        public WebSocketMessageEvent(WebSocketMessage message) {
            super();
            this.webSocketMessage = message;
        }

        WebSocketMessage getWebSocketMessage() {
            return this.webSocketMessage;
        }
    }

}
