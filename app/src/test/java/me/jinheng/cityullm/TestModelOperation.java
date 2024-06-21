package me.jinheng.cityullm;

import org.junit.Test;

import me.jinheng.cityullm.models.ModelOperation;

public class TestModelOperation {

    @Test
    public void testGetModelInfo() {
        System.out.println(ModelOperation.getAllSupportModels().toString());
    }

}
