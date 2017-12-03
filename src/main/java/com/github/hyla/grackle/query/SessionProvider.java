package com.github.hyla.grackle.query;

import org.hibernate.Session;

public interface SessionProvider {

    Session getSession();
}
