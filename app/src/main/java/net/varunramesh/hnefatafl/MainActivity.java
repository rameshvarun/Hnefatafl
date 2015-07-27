package net.varunramesh.hnefatafl;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.cocosw.bottomsheet.BottomSheet;
import com.gc.materialdesign.views.ButtonFloat;
import com.github.florent37.materialviewpager.MaterialViewPager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.example.games.basegameutils.BaseGameUtils;

import net.varunramesh.hnefatafl.simulator.GameState;

public class MainActivity extends BaseGameActivity {

    private MaterialViewPager mViewPager;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;

    private final static int RC_SELECT_PLAYERS = 10000;
    private final static int RC_SIGN_IN = 9001;
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Drawable tapestry = getResources().getDrawable(R.drawable.tapestry);
        final Palette tapestryPalette = Palette.generate(BitmapFactory.decodeResource(getResources(), R.drawable.tapestry));

        setTitle("");

        mViewPager = (MaterialViewPager) findViewById(R.id.materialViewPager);
        toolbar = mViewPager.getToolbar();
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (toolbar != null) {
            setSupportActionBar(toolbar);

            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayUseLogoEnabled(false);
                actionBar.setHomeButtonEnabled(true);
            }
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, 0, 0);
        mDrawer.setDrawerListener(mDrawerToggle);

        mViewPager.getViewPager().setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return new RecyclerViewFragment();
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return "Ongoing Games";
                    case 1:
                        return "Match History";
                }
                return "";
            }
        });

        int defaultColor = getResources().getColor(R.color.buff);
        mViewPager.setColor(tapestryPalette.getVibrantColor(defaultColor), 0);
        mViewPager.setImageDrawable(tapestry, 0);

        mViewPager.getViewPager().setOffscreenPageLimit(mViewPager.getViewPager().getAdapter().getCount());
        mViewPager.getPagerTitleStrip().setViewPager(mViewPager.getViewPager());

        mViewPager.getViewPager().setCurrentItem(0);

        //Intent intent = new Intent(this, PlayerActivity.class);
        //startActivity(intent);

        final BottomSheet.Builder bottomsheet = new BottomSheet.Builder(this)
            .title("Create a New Game...")
            .sheet(R.menu.menu_new_game)
            .listener((DialogInterface dialog, int item) -> {
                Intent intent;
                GameState gameState;
                switch (item) {
                    case R.id.action_pass_and_play:
                        gameState = new GameState(GameState.GameType.PASS_AND_PLAY);
                        intent = new Intent(this, PlayerActivity.class);
                        intent.putExtra("GameState", gameState.toJson().toString());
                        startActivity(intent);
                        break;
                    case R.id.action_online_match:
                        if(!isSignedIn()) {
                            beginUserInitiatedSignIn();
                        } else {
                            intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(getApiClient(), 1, 1, true);
                            startActivityForResult(intent, RC_SELECT_PLAYERS);
                        }
                        break;
                    case R.id.action_player_vs_ai:
                        gameState = new GameState(GameState.GameType.PLAYER_VS_AI);
                        intent = new Intent(this, PlayerActivity.class);
                        intent.putExtra("GameState", gameState.toJson().toString());
                        startActivity(intent);
                        break;
                }
            });

        final ButtonFloat newGame = (ButtonFloat)findViewById(R.id.newgame);
        newGame.setOnClickListener((View v) -> {
            bottomsheet.show();
        });
    }

    @Override
    public void onSignInFailed() {
        Log.e(TAG, "Sign in has failed.");
    }

    @Override
    public void onSignInSucceeded() {
        Log.d(TAG, "Sign in has succeed.");
    }
}
