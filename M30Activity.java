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

import org.strongswan.android.data.VpnProfile;
import org.strongswan.android.data.VpnProfileDataSource;
import org.strongswan.android.data.VpnProfileSource;
import org.strongswan.android.data.VpnType;
import org.strongswan.android.logic.VpnStateService;

import java.util.List;

public class M30Activity extends Activity
        implements VpnStateService.VpnStateListener {

    private static final int VPN_PREPARE = 3001;

    /*
     * M30 VPN profile
     */
    private static final String PROFILE_NAME = "M30 VPN";

    /*
     * Default settings
     */
    private static final String DEFAULT_SERVER = "149.50.208.98";
    private static final String DEFAULT_IDENTITY = "pointtoserver.com";
    private static final String DEFAULT_DNS = "8.8.8.8";

    /*
     * Background colors
     */
    private static final int BG_DISCONNECTED =
            Color.rgb(11, 18, 32);

    private static final int BG_CONNECTING =
            Color.rgb(48, 38, 12);

    private static final int BG_CONNECTED =
            Color.rgb(8, 43, 29);

    /*
     * Button colors
     */
    private static final int BUTTON_DISCONNECTED =
            Color.rgb(45, 116, 190);

    private static final int BUTTON_CONNECTING =
            Color.rgb(190, 135, 25);

    private static final int BUTTON_CONNECTED =
            Color.rgb(28, 155, 92);

    /*
     * Text colors
     */
    private static final int TEXT_WHITE =
            Color.rgb(245, 247, 250);

    private static final int TEXT_MUTED =
            Color.rgb(154, 167, 184);

    private static final int TEXT_CONNECTING =
            Color.rgb(255, 215, 90);

    private static final int TEXT_CONNECTED =
            Color.rgb(92, 225, 150);

    /*
     * UI
     */
    private LinearLayout root;

    private EditText server;
    private EditText identity;
    private EditText username;
    private EditText password;

    private Button save;
    private Button connect;

    private TextView status;

    /*
     * VPN service
     */
    private VpnStateService vpnService;

    private boolean bound = false;

    /*
     * Current M30 VPN profile
     */
    private VpnProfile profile;


    /*
     * ============================================================
     * SERVICE CONNECTION
     * ============================================================
     */

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


    /*
     * ============================================================
     * ACTIVITY CREATE
     * ============================================================
     */

    @Override
    protected void onCreate(Bundle state) {

        super.onCreate(state);

        getWindow().setStatusBarColor(
                BG_DISCONNECTED
        );

        getWindow().setNavigationBarColor(
                BG_DISCONNECTED
        );

        buildUi();

        /*
         * Load saved M30 VPN settings
         */
        loadSavedProfile();

        /*
         * Bind to real strongSwan VPN service
         */
        Intent serviceIntent =
                new Intent(
                        this,
                        VpnStateService.class
                );

        bindService(
                serviceIntent,
                connection,
                Context.BIND_AUTO_CREATE
        );
    }


    /*
     * ============================================================
     * BUILD UI
     * ============================================================
     */

    private void buildUi() {

        root = new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                dp(22),
                dp(25),
                dp(22),
                dp(20)
        );

        root.setGravity(
                Gravity.TOP
        );

        root.setBackgroundColor(
                BG_DISCONNECTED
        );


        /*
         * APP NAME
         */

        TextView title =
                text(
                        "M30 VPN",
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


        /*
         * SUBTITLE
         */

        TextView subtitle =
                text(
                        "IKEv2 Secure Connection",
                        15,
                        false
                );

        subtitle.setTextColor(
                TEXT_MUTED
        );

        LinearLayout.LayoutParams subtitleParams =
                lp(-1, -2);

        subtitleParams.bottomMargin =
                dp(18);

        root.addView(
                subtitle,
                subtitleParams
        );


        /*
         * ========================================================
         * SERVER
         * ========================================================
         */

        server =
                field("Server");

        server.setText(
                DEFAULT_SERVER
        );

        server.setSingleLine(true);

        root.addView(
                server,
                lp(-1, dp(54))
        );


        /*
         * ========================================================
         * SERVER IDENTITY
         * ========================================================
         */

        identity =
                field("Server Identity");

        identity.setText(
                DEFAULT_IDENTITY
        );

        identity.setSingleLine(true);

        identity.setInputType(
                InputType.TYPE_CLASS_TEXT
                        |
                InputType.TYPE_TEXT_VARIATION_NORMAL
        );

        root.addView(
                identity,
                lp(-1, dp(54))
        );


        /*
         * ========================================================
         * USERNAME
         * ========================================================
         */

        username =
                field("Username");

        username.setSingleLine(true);

        username.setInputType(
                InputType.TYPE_CLASS_TEXT
                        |
                InputType.TYPE_TEXT_VARIATION_NORMAL
        );

        root.addView(
                username,
                lp(-1, dp(54))
        );


        /*
         * ========================================================
         * PASSWORD
         * ========================================================
         */

        password =
                field("Password");

        password.setSingleLine(true);

        password.setInputType(
                InputType.TYPE_CLASS_TEXT
                        |
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        root.addView(
                password,
                lp(-1, dp(54))
        );


        /*
         * ========================================================
         * SAVE BUTTON
         * ========================================================
         */

        save =
                new Button(this);

        save.setText(
                "SAVE"
        );

        save.setTextSize(
                14
        );

        save.setTextColor(
                TEXT_WHITE
        );

        save.setAllCaps(
                false
        );

        save.setBackgroundColor(
                Color.rgb(55, 75, 105)
        );

        LinearLayout.LayoutParams saveParams =
                lp(-1, dp(50));

        saveParams.topMargin =
                dp(12);

        root.addView(
                save,
                saveParams
        );


        /*
         * SAVE
         */

        save.setOnClickListener(
                new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (saveProfile()) {

                    Toast.makeText(
                            M30Activity.this,
                            "M30 VPN settings saved",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });


        /*
         * ========================================================
         * STATUS
         * ========================================================
         */

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

        LinearLayout.LayoutParams statusParams =
                lp(-1, dp(45));

        statusParams.topMargin =
                dp(14);

        root.addView(
                status,
                statusParams
        );


        /*
         * ========================================================
         * CONNECT BUTTON
         * ========================================================
         */

        connect =
                new Button(this);

        connect.setText(
                "CONNECT"
        );

        connect.setTextSize(
                16
        );

        connect.setTextColor(
                Color.WHITE
        );

        connect.setAllCaps(
                false
        );

        connect.setBackgroundColor(
                BUTTON_DISCONNECTED
        );

        root.addView(
                connect,
                lp(-1, dp(56))
        );


        /*
         * CONNECT / DISCONNECT
         */

        connect.setOnClickListener(
                new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                toggleConnection();
            }
        });


        /*
         * SHOW UI
         */

        setContentView(root);
    }


    /*
     * ============================================================
     * LOAD SAVED PROFILE
     * ============================================================
     */

    private void loadSavedProfile() {

        VpnProfileDataSource dataSource =
                new VpnProfileSource(this);

        try {

            dataSource.open();

            List<VpnProfile> profiles =
                    dataSource.getAllVpnProfiles();

            if (profiles != null) {

                for (VpnProfile p : profiles) {

                    if (p == null) {
                        continue;
                    }

                    String name =
                            p.getName();

                    /*
                     * Accept both the new name and the old M30
                     * profile so previous settings are not lost.
                     */

                    if (PROFILE_NAME.equals(name)
                            || "M30".equals(name)) {

                        profile = p;

                        /*
                         * Server
                         */

                        if (p.getGateway() != null
                                && !p.getGateway()
                                .trim()
                                .isEmpty()) {

                            server.setText(
                                    p.getGateway()
                            );
                        }


                        /*
                         * Identity
                         */

                        if (p.getRemoteId() != null
                                && !p.getRemoteId()
                                .trim()
                                .isEmpty()) {

                            identity.setText(
                                    p.getRemoteId()
                            );
                        }


                        /*
                         * Username
                         */

                        if (p.getUsername() != null) {

                            username.setText(
                                    p.getUsername()
                            );
                        }


                        /*
                         * Password
                         */

                        if (p.getPassword() != null) {

                            password.setText(
                                    p.getPassword()
                            );
                        }

                        break;
                    }
                }
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Could not load saved VPN settings",
                    Toast.LENGTH_SHORT
            ).show();

        } finally {

            dataSource.close();
        }
    }


    /*
     * ============================================================
     * SAVE PROFILE
     * ============================================================
     */

    private boolean saveProfile() {

        String gateway =
                server.getText()
                        .toString()
                        .trim();

        String remoteIdentity =
                identity.getText()
                        .toString()
                        .trim();

        String user =
                username.getText()
                        .toString()
                        .trim();

        String pass =
                password.getText()
                        .toString();


        /*
         * SERVER VALIDATION
         */

        if (gateway.isEmpty()) {

            server.requestFocus();

            Toast.makeText(
                    this,
                    "Enter Server",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }


        /*
         * IDENTITY VALIDATION
         */

        if (remoteIdentity.isEmpty()) {

            identity.requestFocus();

            Toast.makeText(
                    this,
                    "Enter Server Identity",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }


        /*
         * USERNAME VALIDATION
         */

        if (user.isEmpty()) {

            username.requestFocus();

            Toast.makeText(
                    this,
                    "Enter Username",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }


        /*
         * PASSWORD VALIDATION
         */

        if (pass.isEmpty()) {

            password.requestFocus();

            Toast.makeText(
                    this,
                    "Enter Password",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }


        /*
         * REAL strongSwan profile database
         */

        VpnProfileDataSource dataSource =
                new VpnProfileSource(this);

        try {

            dataSource.open();


            /*
             * Find existing M30 VPN profile
             */

            if (profile == null) {

                List<VpnProfile> profiles =
                        dataSource.getAllVpnProfiles();

                if (profiles != null) {

                    for (VpnProfile p : profiles) {

                        if (p == null) {
                            continue;
                        }

                        String name =
                                p.getName();

                        if (PROFILE_NAME.equals(name)
                                || "M30".equals(name)) {

                            profile = p;

                            break;
                        }
                    }
                }
            }


            /*
             * Create profile if needed
             */

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


            /*
             * ====================================================
             * SAVE ALL SETTINGS
             * ====================================================
             */

            /*
             * Profile name
             */

            profile.setName(
                    PROFILE_NAME
            );


            /*
             * VPN type
             */

            profile.setVpnType(
                    VpnType.IKEV2_EAP
            );


            /*
             * Server
             */

            profile.setGateway(
                    gateway
            );


            /*
             * Server Identity
             */

            profile.setRemoteId(
                    remoteIdentity
            );


            /*
             * Username
             */

            profile.setUsername(
                    user
            );


            /*
             * Password
             */

            profile.setPassword(
                    pass
            );


            /*
             * DNS
             */

            profile.setDnsServers(
                    DEFAULT_DNS
            );


            /*
             * Block IPv6
             */

            profile.setSplitTunneling(
                    VpnProfile.SPLIT_TUNNELING_BLOCK_IPV6
            );


            /*
             * ====================================================
             * WRITE TO DATABASE
             * ====================================================
             */

            if (profile.getId() > 0) {

                boolean updated =
                        dataSource.updateVpnProfile(
                                profile
                        );

                if (!updated) {

                    Toast.makeText(
                            this,
                            "Could not update VPN profile",
                            Toast.LENGTH_LONG
                    ).show();

                    return false;
                }

            } else {

                VpnProfile inserted =
                        dataSource.insertProfile(
                                profile
                        );

                if (inserted == null) {

                    Toast.makeText(
                            this,
                            "Could not save VPN profile",
                            Toast.LENGTH_LONG
                    ).show();

                    return false;
                }

                profile = inserted;
            }


            /*
             * Put the saved values back into UI
             */

            server.setText(
                    profile.getGateway()
            );

            identity.setText(
                    profile.getRemoteId()
            );

            username.setText(
                    profile.getUsername()
            );

            password.setText(
                    profile.getPassword()
            );


            return true;

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Save error: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();

            return false;

        } finally {

            dataSource.close();
        }
    }


    /*
     * ============================================================
     * CONNECT / DISCONNECT
     * ============================================================
     */

    private void toggleConnection() {

        if (!bound || vpnService == null) {

            Toast.makeText(
                    this,
                    "VPN service is not ready",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        VpnStateService.State state =
                vpnService.getState();


        /*
         * Disconnect if currently connected/connecting
         */

        if (state == VpnStateService.State.CONNECTED
                || state == VpnStateService.State.CONNECTING
                || state == VpnStateService.State.DISCONNECTING) {

            vpnService.disconnect();

            return;
        }


        /*
         * SAVE FIRST
         *
         * This means any manual changes to:
         * Server
         * Identity
         * Username
         * Password
         *
         * are saved before connection.
         */

        if (!saveProfile()) {

            return;
        }


        /*
         * Ask Android for VPN permission
         */

        prepareVpnService();
    }


    /*
     * ============================================================
     * VPN PREPARATION
     * ============================================================
     */

    private void prepareVpnService() {

        Intent intent;

        try {

            intent =
                    VpnService.prepare(this);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "VPN cannot be prepared",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        /*
         * Android permission required
         */

        if (intent != null) {

            try {

                startActivityForResult(
                        intent,
                        VPN_PREPARE
                );

            } catch (Exception e) {

                Toast.makeText(
                        this,
                        "Android VPN service is unavailable",
                        Toast.LENGTH_LONG
                ).show();
            }

        } else {

            /*
             * Permission already granted
             */

            startM30Vpn();
        }
    }


    /*
     * ============================================================
     * START REAL strongSwan IKEv2 VPN
     * ============================================================
     */

    private void startM30Vpn() {

        /*
         * Make sure profile exists
         */

        if (profile == null) {

            if (!saveProfile()) {

                return;
            }
        }


        /*
         * Make sure service is connected
         */

        if (vpnService == null) {

            Toast.makeText(
                    this,
                    "VPN service is not ready",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        try {

            /*
             * strongSwan 6.1.0 expects a Bundle here,
             * not a VpnProfile object.
             */

            Bundle bundle =
                    new Bundle();


            /*
             * Profile UUID
             */

            bundle.putString(
                    VpnProfileDataSource.KEY_UUID,
                    profile.getUUID().toString()
            );


            /*
             * Password
             *
             * This is passed to the real Charon/strongSwan
             * VPN service.
             */

            bundle.putString(
                    VpnProfileDataSource.KEY_PASSWORD,
                    profile.getPassword()
            );


            /*
             * Start a fresh connection
             */

            vpnService.connect(
                    bundle,
                    true
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "VPN connection error: "
                            + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    /*
     * ============================================================
     * ACTIVITY RESULT
     * ============================================================
     */

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );


        if (requestCode == VPN_PREPARE) {

            if (resultCode == RESULT_OK) {

                startM30Vpn();

            } else {

                setDisconnectedUi();

                Toast.makeText(
                        this,
                        "VPN permission was not granted",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }


    /*
     * ============================================================
     * strongSwan STATE LISTENER
     * ============================================================
     */

    @Override
    public void stateChanged() {

        runOnUiThread(
                new Runnable() {

                    @Override
                    public void run() {

                        refreshState();
                    }
                }
        );
    }


    /*
     * ============================================================
     * REFRESH STATE
     * ============================================================
     */

    private void refreshState() {

        if (vpnService == null) {

            setDisconnectedUi();

            return;
        }


        VpnStateService.State state =
                vpnService.getState();


        if (state ==
                VpnStateService.State.CONNECTED) {

            setConnectedUi();

        } else if (
                state ==
                VpnStateService.State.CONNECTING) {

            setConnectingUi();

        } else {

            setDisconnectedUi();
        }
    }


    /*
     * ============================================================
     * CONNECTED UI
     * ============================================================
     */

    private void setConnectedUi() {

        root.setBackgroundColor(
                BG_CONNECTED
        );

        getWindow().setStatusBarColor(
                BG_CONNECTED
        );

        getWindow().setNavigationBarColor(
                BG_CONNECTED
        );

        status.setText(
                "Connected"
        );

        status.setTextColor(
                TEXT_CONNECTED
        );

        connect.setText(
                "DISCONNECT"
        );

        connect.setBackgroundColor(
                BUTTON_CONNECTED
        );
    }


    /*
     * ============================================================
     * CONNECTING UI
     * ============================================================
     */

    private void setConnectingUi() {

        root.setBackgroundColor(
                BG_CONNECTING
        );

        getWindow().setStatusBarColor(
                BG_CONNECTING
        );

        getWindow().setNavigationBarColor(
                BG_CONNECTING
        );

        status.setText(
                "Connecting..."
        );

        status.setTextColor(
                TEXT_CONNECTING
        );

        connect.setText(
                "DISCONNECT"
        );

        connect.setBackgroundColor(
                BUTTON_CONNECTING
        );
    }


    /*
     * ============================================================
     * DISCONNECTED UI
     * ============================================================
     */

    private void setDisconnectedUi() {

        root.setBackgroundColor(
                BG_DISCONNECTED
        );

        getWindow().setStatusBarColor(
                BG_DISCONNECTED
        );

        getWindow().setNavigationBarColor(
                BG_DISCONNECTED
        );

        status.setText(
                "Disconnected"
        );

        status.setTextColor(
                TEXT_MUTED
        );

        connect.setText(
                "CONNECT"
        );

        connect.setBackgroundColor(
                BUTTON_DISCONNECTED
        );
    }


    /*
     * ============================================================
     * DESTROY
     * ============================================================
     */

    @Override
    protected void onDestroy() {

        if (bound && vpnService != null) {

            try {

                vpnService.unregisterListener(
                        this
                );

            } catch (Exception ignored) {
            }


            try {

                unbindService(
                        connection
                );

            } catch (Exception ignored) {
            }

            bound = false;
        }

        super.onDestroy();
    }


    /*
     * ============================================================
     * EDIT TEXT FIELD
     * ============================================================
     */

    private EditText field(
            String hint) {

        EditText edit =
                new EditText(this);

        edit.setHint(
                hint
        );

        edit.setTextSize(
                16
        );

        edit.setSingleLine(
                true
        );

        edit.setTextColor(
                TEXT_WHITE
        );

        edit.setHintTextColor(
                TEXT_MUTED
        );

        edit.setPadding(
                dp(14),
                0,
                dp(14),
                0
        );

        edit.setBackgroundColor(
                Color.rgb(25, 35, 50)
        );

        return edit;
    }


    /*
     * ============================================================
     * TEXT
     * ============================================================
     */

    private TextView text(
            String value,
            int size,
            boolean bold) {

        TextView view =
                new TextView(this);

        view.setText(
                value
        );

        view.setTextSize(
                size
        );

        if (bold) {

            view.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );
        }

        return view;
    }


    /*
     * ============================================================
     * LAYOUT PARAMS
     * ============================================================
     */

    private LinearLayout.LayoutParams lp(
            int width,
            int height) {

        return new LinearLayout.LayoutParams(
                width,
                height
        );
    }


    /*
     * ============================================================
     * DP
     * ============================================================
     */

    private int dp(int value) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;

        return (int)
                (value * density + 0.5f);
    }
}
