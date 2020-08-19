package com.github.lihongjie.jaddressparser.util;

import cn.hutool.db.Db;
import cn.hutool.db.Entity;

import java.sql.SQLException;
import java.util.List;

public class DBUtil {


    public static List<Entity> execQuerySQL(String sql) {
        List<Entity> all = null;
        try {
            all = Db.use().query(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return all;
    }


    public static int execUpdateSQL(String sql) {
        int execute = 0;
        try {
            execute = Db.use().execute(sql);
            Db.use().getConnection().commit();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return execute;
    }



    public static void main(String[] args) throws SQLException {

        String sql = "update temp_address t set t.id = t.id where t.id = '1'";
        int execute = Db.use().execute(sql);
        System.out.println(execute);

    }


}
