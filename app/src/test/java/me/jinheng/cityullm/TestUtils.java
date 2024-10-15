package me.jinheng.cityullm;

import org.junit.Test;

import me.jinheng.cityullm.models.Utils;

public class TestUtils {

    @Test
    public void testGetMemorySize() {
        long res = Utils.getTotalMemory();
        System.out.println(res);
    }

}
