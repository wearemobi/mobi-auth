// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.edge;

import java.util.Map;

public record BioNode(SignatureNode signature, Map<String, String> urls) {}
