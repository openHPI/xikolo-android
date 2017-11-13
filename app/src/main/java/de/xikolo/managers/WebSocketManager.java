package de.xikolo.managers;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import de.xikolo.config.Config;
import de.xikolo.events.LoginEvent;
import de.xikolo.events.LogoutEvent;
import de.xikolo.events.NetworkStateEvent;
import de.xikolo.events.base.Event;
import de.xikolo.models.WebSocketMessage;
import de.xikolo.utils.NetworkUtil;
import de.xikolo.utils.ParserUtil;

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
            headers.put(Config.HEADER_AUTH, Config.HEADER_AUTH_VALUE_PREFIX + token);

            webSocketClient = new WebSocketClient(uri, new Draft_6455(), headers, 0) {
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
                    EventBus.getDefault().post(new WebSocketMessageEvent(message));
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
                SSLSocketFactory factory = sc.getSocketFactory();
                webSocketClient.setSocket(factory.createSocket());
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
        if (event.isOnline() && UserManager.isAuthorized()) {
            initConnection(UserManager.getToken());
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onLoginEvent(LoginEvent event) {
        if (NetworkUtil.isOnline()) {
            initConnection(UserManager.getToken());
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
            this.webSocketMessage = ParserUtil.parse(message, WebSocketMessage.class);
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
