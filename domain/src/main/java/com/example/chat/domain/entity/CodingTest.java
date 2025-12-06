package com.example.chat.domain.entity;

class Solution {
	public int solution(int n) {
		// c_n = sig(i = 0 -> i = n-1) {c_i * c_n+i-1}
		int[] dp = new int[n + 1];
		dp[0] = 1;

		for (int i = 1; i <= n; i++) {
			for (int j = 0; j < i; j++) {
				dp[i] += dp[j] * dp[i - j - 1];
			}
		}
		return dp[n];
	}
}

public class CodingTest {
	public static void main(String[] args) {

	}
}