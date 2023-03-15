package fi.nls.dbquality.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.mockito.ArgumentMatcher;


public class UUIDListMatcher implements ArgumentMatcher<Object[]> {
    private final List<UUID> expected;

    public UUIDListMatcher(List<UUID> expected) {
        this(expected, 1);
    }

    public UUIDListMatcher(List<UUID> expected, int count) {
        this.expected = new ArrayList<>();
        while (count-- > 0) {
            this.expected.addAll(expected);
        }
    }

    @Override
    public Class<?> type() {
        return Object[].class;
    }

    @Override
    public boolean matches(Object[] argument) {
        if (this.expected == null && argument == null) {
            return true;
        }
        if (this.expected == null || argument == null) {
            return false;
        }
        if (this.expected.size() != argument.length) {
            return false;
        }
        for (int i = 0; i < this.expected.size(); i++) {
            if (!this.expected.get(i).equals(argument[i])) {
                return false;
            }
        }
        return true;
    }
}
