package com.nolanlawson.keepscore;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.widget.LinearLayout;

public class LandscapeGameActivity extends GameActivity {
    
    private LinearLayout rowLayout2;
    private LinearLayout rowLayout3;
    private LinearLayout rowLayout4;
    
    /**
     * [customizacao] Android 17+: a Activity pode ser criada ainda com configuracao retrato,
     * antes do sistema aplicar o screenOrientation="landscape". Sem isto, o layout retrato
     * (sem game_row_2/3/4 e sem botoes de delta) e inflado e o app aborta com NPE.
     * Forca os recursos desta Activity a usarem a configuracao landscape (layout-land).
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        Configuration current = newBase.getResources().getConfiguration();
        if (current.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            Configuration override = new Configuration(current);
            override.orientation = Configuration.ORIENTATION_LANDSCAPE;
            applyOverrideConfiguration(override);
        }
    }

    @Override
    public void hideAbsentPlayers() {
        
        
        // set which rows are visible based on how many players there are
        rowLayout2 = (LinearLayout) findViewById(R.id.game_row_2);
        rowLayout3 = (LinearLayout) findViewById(R.id.game_row_3);
        rowLayout4 = (LinearLayout) findViewById(R.id.game_row_4);
        rowLayout2.setVisibility(playerScores.size() > 2 ? View.VISIBLE : View.GONE);
        rowLayout3.setVisibility(playerScores.size() > 4 ? View.VISIBLE : View.GONE);
        rowLayout4.setVisibility(playerScores.size() > 6 ? View.VISIBLE : View.GONE);
        
        if (playerScores.size() == 3) {
            // hide the "fourth" player
            getPlayerScoreView(R.id.player_4).setVisibility(View.INVISIBLE);
        } else if (playerScores.size() == 5) {
            // hide the "sixth" player
            getPlayerScoreView(R.id.player_6).setVisibility(View.INVISIBLE);
        } else if (playerScores.size() == 7) {
            // hide the "eighth" player
            getPlayerScoreView(R.id.player_8).setVisibility(View.INVISIBLE);
        }
    }
    
    @Override
    public boolean getShouldShowOnscreenDeltaButtons() {
        // only show the onscreen delta buttons if space allows
        return playerScores.size() <= getResources().getInteger(
                R.integer.max_players_for_onscreen_delta_buttons);
    }
}