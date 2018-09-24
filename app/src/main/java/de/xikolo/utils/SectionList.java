package de.xikolo.utils;

import java.util.ArrayList;
import java.util.List;

public class SectionList<H, S extends List> {

    private List<H> header;

    private List<S> sections;

    public SectionList() {
        super();
        header = new ArrayList<>();
        sections = new ArrayList<>();
    }

    public void add(H header, S section) {
        this.header.add(header);
        this.sections.add(section);
    }

    public void clear() {
        this.header.clear();
        this.sections.clear();
    }

    public boolean isHeader(int position) {
        int i = 0;

        for (List section : sections) {
            i += section.size() + 1;
            if (position == i - section.size() - 1) {
                return true;
            }
        }

        return false;
    }

    public int size() {
        int i = header.size();

        for (List section : sections) {
            i += section.size();
        }

        return i;
    }

    public Object getItem(int position) {
        int i = 0;
        int j = 0;

        for (List section : sections) {
            i += section.size() + 1;
            if (position == i - section.size() - 1) {
                return header.get(j);
            } else if (position >= i - section.size() && position < i) {
                return sections.get(j).get(position - (i - section.size()));
            }
            j++;
        }

        return null;
    }

}
