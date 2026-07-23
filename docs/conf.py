#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# SPDX-License-Identifier: EPL-1.0
##############################################################################
# Copyright (c) 2018 The Linux Foundation and others.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
##############################################################################

from docs_conf.conf import *

# ponytail: ignore permanently-dead external links (retired hosts/tutorials)
# so docs-linkcheck does not fail on pre-existing rot. Drop entries if a live
# replacement URL is found.
linkcheck_ignore = globals().get("linkcheck_ignore", []) + [
    r"https://gerrit\.fd\.io/.*",
    r"https://wiki\.fd\.io/.*",
    r"https://wiki\.opendaylight\.org/.*",
    r"https://git\.opendaylight\.org/gerrit/gitweb.*",
    r"https://github\.com/davidmeyer/lig",
    r"https://github\.com/OpenOverlayRouter/oor",
    r"https://tools\.ietf\.org/.*",
    r"https://www\.cisco\.com/.*",
    r"https://www\.opendaylight\.org/downloads",
]
