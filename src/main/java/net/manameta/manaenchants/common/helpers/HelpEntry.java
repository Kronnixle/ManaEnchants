package net.manameta.manaenchants.common.helpers;

import java.util.List;

public record HelpEntry(
        HelpID helpID,                  // For easier access
        String formatKey,               // For getting the command format key... Format could be different per locale.
        List<String> aliases,           // from config
        String shortDescriptionKey,     // used in list view
        List<String> detailedHelpKeys   // used in /... help <sub-arg>
) {}