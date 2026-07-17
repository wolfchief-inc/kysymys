package net.unit8.kysymys.example.fizzbuzz;

import net.unit8.kysymys.scorer.KysymysTestLauncher;

/**
 * 自己採点のエントリポイント。
 *
 * <p>{@code KysymysTestLauncher} が {@link FizzBuzzTest} を JUnit Platform で走らせ、
 * 成功したテストの割合 (0〜100) を stdout に出す。研修参加者は提出前に
 * {@code mvn test-compile exec:java} で自分の点数を確認できる。
 *
 * <p>サーバ側採点は kysymys-app には存在しない (scorer はローカル実行)。
 */
public final class Scoring {
    private Scoring() {
    }

    public static void main(String[] args) {
        KysymysTestLauncher.run(FizzBuzzTest.class);
    }
}
