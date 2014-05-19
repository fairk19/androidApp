package ru.mail.app;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anton on 19.05.14.
 */
public class MenuAdapter {

    public static List<MenuItem> ITEMS = new ArrayList<MenuItem>();

    MenuAdapter(List<String> strings) {
        int i = 0;
        while (!strings.isEmpty()) {
            ITEMS.add(new MenuItem( String.valueOf(i),strings.remove(i)));
        }
    }

    public class MenuItem {
        public String id;
        public String content;

        public MenuItem(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }

}
