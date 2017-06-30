package com.zavtech.morpheus.viz.table;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.LocalDate;
import javax.swing.JFrame;

import com.zavtech.morpheus.viz.jfree.TestProvider;
import com.zavtech.morpheus.frame.DataFrame;

public class TestTable {

    public static void main(String[] args) throws Exception {
        final DataFrame<LocalDate,String> frame = TestProvider.getQuotes("AAPL");
        final JFrame window = new JFrame("Test DataFrame Table");
        window.getContentPane().setLayout(new BorderLayout(0,0));
        window.getContentPane().add(new DataFrameTable(frame));
        window.setSize(new Dimension(1024,768));
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
    }
}
