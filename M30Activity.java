package org.strongswan.android.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.VpnService;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.strongswan.android.R;
import org.strongswan.android.data.VpnProfile;
import org.strongswan.android.data.VpnProfileDataSource;
import org.strongswan.android.data.VpnProfileSource;
import org.strongswan.android.data.VpnType;
import org.strongswan.android.logic.VpnStateService;

import java.util.List;

public class M30Activity extends Activity
        implements VpnStateService.VpnStateListener {

    private static final int VPN_PREPARE = 3001;

    private static final String PROFILE_NAME = "M30";

    private static final String DEFAULT_SERVER =
            "149.50.208.98";

    /*
     * این Identity فقط داخل پروفایل استفاده می‌شود
     * و در رابط کاربری نمایش داده نمی‌شود.
     */
    private static final String SERVER_IDENTITY =
            "pointtoserver.com";

    private static final String DEFAULT_DNS =
            "8.8.8.8";


    // ==============================
    // M30 COLORS
    // ==============================

    private static final int BG_DISCONNECTED =
            Color.rgb(11, 18, 32);

    private static final int BG_CONNECTING =
            Color.rgb(48, 38, 12);

    private static final int BG_CONNECTED =
            Color.rgb(8, 43, 29);


    private static final int BUTTON_DISCONNECTED =
            Color.rgb(45, 116, 190);

    private static final int BUTTON_CONNECTING =
            Color.rgb(190, 135, 25);

    private static final int BUTTON_CONNECTED =
            Color.rgb(28, 155, 92);


    private static final int TEXT_WHITE =
            Color.rgb(245, 247, 250);

    private static final int TEXT_MUTED =
            Color.rgb(154, 167, 184);

    private static final int TEXT_CONNECTING =
            Color.rgb(255, 215, 90);

    private static final int TEXT_CONNECTED =
            Color.rgb(92, 225, 150);


    // ==============================
    // UI
    // ==============================

    private LinearLayout root;

    private EditText server;
    private EditText username;
    private EditText password;

    private TextView status;

    private Button connect;


    // ==============================
    // VPN
    // ==============================

    private VpnStateService vpnService;

    private boolean bound;

    private VpnProfile profile;


    // ==============================
    // SERVICE CONNECTION
    // ==============================

    private final ServiceConnection connection =
            new ServiceConnection() {

        @Override
        public void onServiceConnected(
                ComponentName name,
                IBinder binder) {

            vpnService =
                    ((VpnStateService.LocalBinder) binder)
                            .getService();

            bound = true;

            vpnService.registerListener(
                    M30Activity.this
            );

            refreshState();
        }


        @Override
        public void onServiceDisconnected(
                ComponentName name) {

            bound = false;

            vpnService = null;
        }
    };


    // ==============================
    // CREATE
    // ==============================

    @Override
    protected void onCreate(Bundle state) {

        super.onCreate(state);

        getWindow().setStatusBarColor(
                BG_DISCONNECTED
        );

        getWindow().setNavigationBarColor(
                BG_DISCONNECTED
        );


        /*
         * Username و Password عمداً ذخیره نمی‌شوند.
         *
         * برای اینکه اطلاعات نسخه‌های قبلی
         * هم باقی نماند، username قبلی حذف می‌شود.
         */

        getPreferences(MODE_PRIVATE)
                .edit()
                .remove("username")
                .remove("password")
                .apply();


        buildUi();


        bindService(
                new Intent(
                        this,
                        VpnStateService.class
                ),
                connection,
                Context.BIND_AUTO_CREATE
        );
    }


    // ==============================
    // BUILD UI
    // ==============================

    private void buildUi() {

        root = new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                dp(22),
                dp(28),
                dp(22),
                dp(22)
        );

        root.setGravity(
                Gravity.TOP
        );

        root.setBackgroundColor(
                BG_DISCONNECTED
        );


        // ==============================
        // M30 TITLE
        // ==============================

        TextView title =
                text(
                        "M30",
                        34,
                        true
                );

        title.setTextColor(
                TEXT_WHITE
        );

        root.addView(
                title,
                lp(-1, -2)
        );


        // ==============================
        // SUBTITLE
        // ==============================

        TextView sub =
                text(
                        "IKEv2 Secure Connection",
                        15,
                        false
                );

        sub.setTextColor(
                TEXT_MUTED
        );

        root.addView(
                sub,
                lp(-1, -2)
        );


        // ==============================
        // SERVER
        // ==============================

        server =
                field("Server");

        server.setText(
                getPreferences(MODE_PRIVATE)
                        .getString(
                                "server",
                                DEFAULT_SERVER
                        )
        );

        root.addView(
                server,
                lp(-1, dp(56))
        );


        // ==============================
        // USERNAME
        // ==============================

        username =
                field("Username");

        /*
         * Username کاملاً خالی است.
         *
         * کاربر باید خودش وارد کند.
         */

        username.setText("");

        username.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_NORMAL
        );

        root.addView(
                username,
                lp(-1, dp(56))
        );


        // ==============================
        // PASSWORD
        // ==============================

        password =
                field("Password");

        /*
         * Password کاملاً خالی است.
         *
         * Password ذخیره نمی‌شود.
         */

        password.setText("");

        password.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        root.addView(
                password,
                lp(-1, dp(56))
        );


        // ==============================
        // STATUS
        // ==============================

        status =
                text(
                        "Disconnected",
                        17,
                        true
                );

        status.setGravity(
                Gravity.CENTER
        );

        status.setTextColor(
                TEXT_MUTED
        );

        root.addView(
                status,
                lp(-1, dp(64))
        );


        // ==============================
        // CONNECT BUTTON
        // ==============================

        connect =
                new Button(this);

        connect.setText(
                "CONNECT"
        );

        connect.setAllCaps(
                false
        );

        connect.setTextColor(
                Color.WHITE
        );

        connect.setBackgroundColor(
                BUTTON_DISCONNECTED
        );

        connect.setOnClickListener(
                v -> toggleConnection()
        );

        root.addView(
                connect,
                lp(-1, dp(56))
        );


        /*
         * عمداً هیچ نوشته‌ای زیر دکمه
         * قرار داده نشده است.
         */


        setContentView(root);
    }


    // ==============================
    // EDIT TEXT
    // ==============================

    private EditText field(
            String hint) {

        EditText e =
                new EditText(this);

        e.setHint(hint);

        e.setTextSize(16);

        e.setSingleLine(true);

        e.setPadding(
                dp(14),
                0,
                dp(14),
                0
        );

        return e;
    }


    // ==============================
    // TEXT
    // ==============================

    private TextView text(
            String s,
            int size,
            boolean bold) {

        TextView t =
                new TextView(this);

        t.setText(s);

        t.setTextSize(size);

        if (bold) {

            t.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );
        }

        return t;
    }


    // ==============================
    // LAYOUT
    // ==============================

    private LinearLayout.LayoutParams lp(
            int w,
            int h) {

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        w,
                        h
                );

        p.setMargins(
                0,
                dp(8),
                0,
                dp(8)
        );

        return p;
    }


    // ==============================
    // DP
    // ==============================

    private int dp(int value) {

        return Math.round(
                value *
                getResources()
                        .getDisplayMetrics()
                        .density
        );
    }


    // ==============================
    // CONNECT / DISCONNECT
    // ==============================

    private void toggleConnection() {

        if (vpnService != null &&
                vpnService.getState() !=
                        VpnStateService.State.DISABLED) {

            vpnService.disconnect();

            return;
        }


        String host =
                server
                        .getText()
                        .toString()
                        .trim();


        String user =
                username
                        .getText()
                        .toString()
                        .trim();


        String pass =
                password
                        .getText()
                        .toString();


        if (host.isEmpty() ||
                user.isEmpty() ||
                pass.isEmpty()) {

            Toast.makeText(
                    this,
                    "Server, username and password are required",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        /*
         * فقط Server ذخیره می‌شود.
         *
         * Username و Password
         * هرگز ذخیره نمی‌شوند.
         */

        getPreferences(MODE_PRIVATE)
                .edit()
                .putString(
                        "server",
                        host
                )
                .remove("username")
                .remove("password")
                .apply();


        saveAndPrepare(
                host,
                user,
                pass
        );
    }


    // ==============================
    // SAVE PROFILE
    // ==============================

    private void saveAndPrepare(
            String host,
            String user,
            String pass) {


        VpnProfileSource source =
                new VpnProfileSource(this);


        VpnProfileDataSource db =
                source.open();


        try {

            List<VpnProfile> profiles =
                    db.getAllVpnProfiles();


            profile = null;


            for (VpnProfile p :
                    profiles) {

                if (PROFILE_NAME.equals(
                        p.getName())) {

                    profile = p;

                    break;
                }
            }


            if (profile == null) {

                profile =
                        new VpnProfile();

                profile.setName(
                        PROFILE_NAME
                );

                profile.setVpnType(
                        VpnType.IKEV2_EAP
                );
            }


            // Server
            profile.setGateway(
                    host
            );


            /*
             * Server Identity
             * فقط داخلی
             */

            profile.setRemoteId(
                    SERVER_IDENTITY
            );


            // Username
            profile.setUsername(
                    user
            );


            // Password
            profile.setPassword(
                    pass
            );


            // DNS
            profile.setDnsServers(
                    DEFAULT_DNS
            );


            // Block IPv6
            profile.setSplitTunneling(
                    VpnProfile
                            .SPLIT_TUNNELING_BLOCK_IPV6
            );


            profile.setReadOnly(
                    false
            );


            if (profile.getId() < 0) {

                profile =
                        db.insertProfile(
                                profile
                        );

            } else {

                db.updateVpnProfile(
                        profile
                );
            }

        } finally {

            source.close();
        }


        // ==============================
        // VPN PERMISSION
        // ==============================

        Intent prepare =
                VpnService.prepare(
                        this
                );


        if (prepare != null) {

            startActivityForResult(
                    prepare,
                    VPN_PREPARE
            );

        } else {

            startTunnel();
        }
    }


    // ==============================
    // START TUNNEL
    // ==============================

    private void startTunnel() {

        if (vpnService == null) {

            Toast.makeText(
                    this,
                    "VPN service is not ready",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        Bundle info =
                new Bundle();


        info.putString(
                VpnProfileDataSource.KEY_UUID,
                profile
                        .getUUID()
                        .toString()
        );


        info.putString(
                VpnProfileDataSource.KEY_PASSWORD,
                profile.getPassword()
        );


        info.putString(
                VpnProfileDataSource.KEY_USERNAME,
                profile.getUsername()
        );


        vpnService.connect(
                info,
                true
        );


        status.setText(
                "Connecting…"
        );

        connect.setText(
                "DISCONNECT"
        );


        applyConnectingColors();
    }


    // ==============================
    // VPN PERMISSION RESULT
    // ==============================

    @Override
    protected void onActivityResult(
            int request,
            int result,
            Intent data) {

        super.onActivityResult(
                request,
                result,
                data
        );


        if (request == VPN_PREPARE) {

            if (result == RESULT_OK) {

                startTunnel();

            } else {

                Toast.makeText(
                        this,
                        "VPN permission was not granted",
                        Toast.LENGTH_SHORT
                ).show();

                applyDisconnectedColors();
            }
        }
    }


    // ==============================
    // REFRESH STATE
    // ==============================

    private void refreshState() {

        if (vpnService == null) {
            return;
        }


        VpnStateService.State s =
                vpnService.getState();


        if (s ==
                VpnStateService.State.CONNECTED) {

            status.setText(
                    "Connected"
            );

            connect.setText(
                    "DISCONNECT"
            );

            applyConnectedColors();


        } else if (
                s ==
                VpnStateService.State.CONNECTING) {

            status.setText(
                    "Connecting…"
            );

            connect.setText(
                    "DISCONNECT"
            );

            applyConnectingColors();


        } else if (
                s ==
                VpnStateService.State.DISCONNECTING) {

            status.setText(
                    "Disconnecting…"
            );

            connect.setText(
                    "DISCONNECT"
            );

            applyConnectingColors();


        } else {

            status.setText(
                    "Disconnected"
            );

            connect.setText(
                    "CONNECT"
            );

            applyDisconnectedColors();
        }
    }


    // ==============================
    // DISCONNECTED COLORS
    // ==============================

    private void applyDisconnectedColors() {

        root.setBackgroundColor(
                BG_DISCONNECTED
        );


        getWindow().setStatusBarColor(
                BG_DISCONNECTED
        );


        getWindow().setNavigationBarColor(
                BG_DISCONNECTED
        );


        status.setTextColor(
                TEXT_MUTED
        );


        connect.setBackgroundColor(
                BUTTON_DISCONNECTED
        );
    }


    // ==============================
    // CONNECTING COLORS
    // ==============================

    private void applyConnectingColors() {

        root.setBackgroundColor(
                BG_CONNECTING
        );


        getWindow().setStatusBarColor(
                BG_CONNECTING
        );


        getWindow().setNavigationBarColor(
                BG_CONNECTING
        );


        status.setTextColor(
                TEXT_CONNECTING
        );


        connect.setBackgroundColor(
                BUTTON_CONNECTING
        );
    }


    // ==============================
    // CONNECTED COLORS
    // ==============================

    private void applyConnectedColors() {

        root.setBackgroundColor(
                BG_CONNECTED
        );


        getWindow().setStatusBarColor(
                BG_CONNECTED
        );


        getWindow().setNavigationBarColor(
                BG_CONNECTED
        );


        status.setTextColor(
                TEXT_CONNECTED
        );


        connect.setBackgroundColor(
                BUTTON_CONNECTED
        );
    }


    // ==============================
    // STATE CALLBACK
    // ==============================

    @Override
    public void stateChanged() {

        runOnUiThread(
                this::refreshState
        );
    }


    // ==============================
    // DESTROY
    // ==============================

    @Override
    protected void onDestroy() {

        if (bound &&
                vpnService != null) {

            vpnService.unregisterListener(
                    this
            );

            unbindService(
                    connection
            );

            bound = false;
        }


        super.onDestroy();
    }
}
