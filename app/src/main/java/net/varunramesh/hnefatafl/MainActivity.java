package net.varunramesh.hnefatafl;

import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;
import com.gc.materialdesign.views.ButtonFloat;
import com.github.florent37.materialviewpager.MaterialViewPager;
import com.github.florent37.materialviewpager.header.HeaderDesign;

public class MainActivity extends AppCompatActivity {

    private MaterialViewPager mViewPager;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;

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

        final BottomSheet.Builder bottomsheet = new BottomSheet.Builder(this)
            .title("Create a New Game...")
            .sheet(R.menu.menu_new_game)
            .listener((DialogInterface dialog, int item) -> {
                switch (item) {
                    case R.id.action_pass_and_play:
                        break;
                    case R.id.action_online_match:
                        break;
                    case R.id.action_player_vs_ai:
                        break;
                }
            });

        final ButtonFloat newGame = (ButtonFloat)findViewById(R.id.newgame);
        newGame.setOnClickListener((View v) -> {
            bottomsheet.show();
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
