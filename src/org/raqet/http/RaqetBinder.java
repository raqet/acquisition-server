/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.http;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

public final class RaqetBinder extends AbstractBinder {
    private final RaqetControl _raqetControll;

    public RaqetBinder(final RaqetControl raquetControll2) {
        _raqetControll = raquetControll2;
    }

    @Override
    protected void configure() {
        bind(_raqetControll).to(RaqetControl.class);
    }
}
