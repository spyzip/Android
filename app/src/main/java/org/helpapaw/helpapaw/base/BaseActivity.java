package org.helpapaw.helpapaw.base;

import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.about.AboutActivity;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.databinding.ActivityBaseBinding;
import org.helpapaw.helpapaw.faq.FAQsView;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.SharingUtils;
import org.helpapaw.helpapaw.utils.Utils;

/**
 * Created by iliyan on 6/22/16
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected ActivityBaseBinding binding;
    private ActionBarDrawerToggle drawerToggle;
    private UserManager userManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, getLayoutId());
        setSupportActionBar(binding.toolbar);
        userManager = Injection.getUserManagerInstance();

        // Adding menu icon to Toolbar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            VectorDrawableCompat indicator =
                    VectorDrawableCompat.create(getResources(), R.drawable.ic_menu, getTheme());
            if (indicator != null) {
                indicator.setTint(ResourcesCompat.getColor(getResources(), android.R.color.white, getTheme()));
            }
            supportActionBar.setHomeAsUpIndicator(indicator);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowTitleEnabled(false);
            binding.toolbarTitle.setText(getToolbarTitle());
        }

        drawerToggle = setupDrawerToggle();
        binding.drawer.addDrawerListener(drawerToggle);

        binding.navView.setNavigationItemSelectedListener(getNavigationItemSelectedListener());
    }

    public NavigationView.OnNavigationItemSelectedListener getNavigationItemSelectedListener() {
        return new NavigationView.OnNavigationItemSelectedListener() {
            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                // Set item in checked state
                // TODO: handle navigation
                switch (menuItem.getItemId()) {
                    case R.id.nav_item_faqs:
                        menuItem.setChecked(false);
                        navigateFAQsSection();
                        break;

                    case R.id.nav_item_feedback:
//                        startActivity(SharingUtils.sendFeedback());
                        SharingUtils.sendFeedbackUsingCompat(BaseActivity.this);
                        menuItem.setChecked(false);
                        break;

                    case R.id.nav_item_about:
                        menuItem.setChecked(true);
                        Intent intent = new Intent(BaseActivity.this, AboutActivity.class);
                        startActivity(intent);
                        break;

                    case R.id.nav_item_sign_out:
                        signOut();
                        break;
                }

                // Closing drawer on item click
                binding.drawer.closeDrawers();
                return true;
            }
        };
    }

    private void signOut() {
        if (Utils.getInstance().hasNetworkConnection()) {
            userManager.logout(new UserManager.LogoutCallback() {
                @Override
                public void onLogoutSuccess() {
                    Snackbar.make(binding.getRoot().findViewById(R.id.fab_add_signal), R.string.txt_logout_succeeded, Snackbar.LENGTH_LONG).show();
                    binding.navView.getMenu().findItem(R.id.nav_item_sign_out).setVisible(false);
                }

                @Override
                public void onLogoutFailure(String message) {
                    Snackbar.make(binding.getRoot().findViewById(R.id.fab_add_signal), String.format(getString(R.string.txt_logout_failed), message), Snackbar.LENGTH_LONG).show();
                }
            });
        } else {
            Snackbar.make(binding.getRoot().findViewById(R.id.fab_add_signal), R.string.txt_no_internet, Snackbar.LENGTH_LONG).show();
        }
    }

    private void navigateFAQsSection() {
        Intent intent = new Intent(this, FAQsView.class);
        startActivity(intent);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this,
                binding.drawer,
                binding.toolbar,
                R.string.drawer_open,
                R.string.drawer_close);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            binding.drawer.openDrawer(GravityCompat.START);
        }
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String userToken = userManager.getUserToken();

        binding.navView.getMenu().findItem(R.id.nav_item_sign_out).setChecked(false);

        if (userToken != null && !userToken.equals("")) {
            binding.navView.getMenu().findItem(R.id.nav_item_sign_out).setVisible(true);
        } else {
            binding.navView.getMenu().findItem(R.id.nav_item_sign_out).setVisible(false);
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    protected abstract String getToolbarTitle();

    protected abstract int getLayoutId();
}
