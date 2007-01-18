xinha_editors = null;
xinha_init    = null;
xinha_config  = null;
xinha_plugins = null;

// This contains the names of textareas we will make into Xinha editors
xinha_init = xinha_init ? xinha_init : function()
{
    xinha_plugins = xinha_plugins ? xinha_plugins :
                    [
                            'CharacterMap',
                            'ContextMenu',
                            'InsertAnchor',
                            'FullScreen',
                            'ListType',
                            'SpellChecker',
                            'Stylist',
                            'SuperClean',
                            'TableOperations'
                            ];
    // THIS BIT OF JAVASCRIPT LOADS THE PLUGINS, NO TOUCHING  :)
    if(!Xinha.loadPlugins(xinha_plugins, xinha_init)) return;

    xinha_config = xinha_config ? xinha_config() : new Xinha.Config();

    xinha_editors = xinha_editors ? xinha_editors : Xinha.makeEditors([ 'text' ], xinha_config, xinha_plugins);

    Xinha.startEditors(xinha_editors);

}

window.onload = xinha_init;
