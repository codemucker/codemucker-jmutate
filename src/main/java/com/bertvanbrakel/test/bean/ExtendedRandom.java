package com.bertvanbrakel.test.bean;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

class ExtendedRandom extends Random {
char nextChar() {
    return (char) next(16);
}

byte nextByte() {
    return (byte) next(8);
}

short nextShort() {
    return (short) next(16);
}

BigDecimal nextBigDecimal() {
    int scale = nextInt();
    return new BigDecimal(nextBigInteger(), scale);
}

BigInteger nextBigInteger() {
    int randomLen = 1 + nextInt(15);
    byte[] bytes = new byte[randomLen];
    nextBytes(bytes);
    return new BigInteger(bytes);
}
}