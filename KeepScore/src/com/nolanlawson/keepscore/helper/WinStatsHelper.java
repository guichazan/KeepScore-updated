package com.nolanlawson.keepscore.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.nolanlawson.keepscore.db.Game;
import com.nolanlawson.keepscore.db.PlayerScore;

/**
 * [customizacao] Estatistica de vitorias por pessoa, considerando todos os jogos salvos.
 *
 * Regras:
 * - vence quem tem a MAIOR pontuacao no jogo; em empate, todos os empatados ganham;
 * - jogos sem nenhuma rodada lancada (historico vazio para todos) nao contam;
 * - jogadores sem nome ("Player 1"...) sao ignorados, pois nao identificam uma pessoa;
 * - nomes iguais ignorando maiusculas/minusculas e espacos nas pontas sao a mesma pessoa.
 *
 * Sem dependencias de Android, para poder ser testada isoladamente.
 */
public class WinStatsHelper {

    public static class PlayerWinStats {
        private final String name;
        private int wins;
        private int gamesPlayed;

        PlayerWinStats(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int getWins() {
            return wins;
        }

        public int getGamesPlayed() {
            return gamesPlayed;
        }
    }

    public static class Result {
        private final List<PlayerWinStats> players;
        private final int gamesCounted;

        Result(List<PlayerWinStats> players, int gamesCounted) {
            this.players = players;
            this.gamesCounted = gamesCounted;
        }

        /** Ordenado por vitorias (desc), depois jogos (asc), depois nome. */
        public List<PlayerWinStats> getPlayers() {
            return players;
        }

        public int getGamesCounted() {
            return gamesCounted;
        }
    }

    public static Result computeWins(List<Game> games) {
        Map<String, PlayerWinStats> byKey = new LinkedHashMap<String, PlayerWinStats>();
        int gamesCounted = 0;

        for (Game game : games) {
            if (game == null || game.getPlayerScores() == null || !hasAnyRound(game)) {
                continue;
            }
            gamesCounted++;

            long maxScore = Long.MIN_VALUE;
            for (PlayerScore playerScore : game.getPlayerScores()) {
                maxScore = Math.max(maxScore, playerScore.getScore());
            }

            for (PlayerScore playerScore : game.getPlayerScores()) {
                String name = normalizeDisplayName(playerScore.getName());
                if (name == null) {
                    continue;
                }
                String key = name.toLowerCase(Locale.ROOT);
                PlayerWinStats stats = byKey.get(key);
                if (stats == null) {
                    stats = new PlayerWinStats(name);
                    byKey.put(key, stats);
                }
                stats.gamesPlayed++;
                if (playerScore.getScore() == maxScore) {
                    stats.wins++;
                }
            }
        }

        List<PlayerWinStats> players = new ArrayList<PlayerWinStats>(byKey.values());
        Collections.sort(players, new Comparator<PlayerWinStats>() {
            @Override
            public int compare(PlayerWinStats a, PlayerWinStats b) {
                if (a.wins != b.wins) {
                    return b.wins - a.wins;
                }
                if (a.gamesPlayed != b.gamesPlayed) {
                    return a.gamesPlayed - b.gamesPlayed;
                }
                return a.name.compareToIgnoreCase(b.name);
            }
        });
        return new Result(players, gamesCounted);
    }

    private static boolean hasAnyRound(Game game) {
        for (PlayerScore playerScore : game.getPlayerScores()) {
            if (playerScore.getHistory() != null && !playerScore.getHistory().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeDisplayName(String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.trim();
        return trimmed.length() == 0 ? null : trimmed;
    }
}
