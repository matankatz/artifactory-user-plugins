Artifactory Dynamic Remote Repository User Plugin
=======================================

This plugin receives download request with url query param and fetches the artifact to Artifactory and then delivering the content to the user.

Pre-Requisites
--------------

To allow the user-plugin to serve anonymous download requests, the requested local repository should be enabled for anonymous deploy.

Dependencies
------------

The plugin requires third-party dependency in order to run:

[http-builder](https://jcenter.bintray.com/org/codehaus/groovy/modules/http-builder/http-builder/0.7.2/http-builder-0.7.2.jar)

To install the dependencies, create the `$ARTIFACTORY_HOME/etc/plugins/lib`
directory, and place the above jar in it.

Logging
-------

To enable logging for the plugin, add the below logger to your
`$ARTIFACTORY_HOME/logback.xml`:

```xml
<logger name="DynamicRemoteRepository">
    <level value="info"/>
</logger>
```

Executing and Parameters
------------------------

This plugin will be activated with each download request to Artifactory, but only requests to local repository with URL query param will trigger the remote fetching.

`curl "http://localhost:8081/artifactory/local-repo/a/b/c.zip?url=http://somesite.com"`

Available query parameters are:

- `url` - Mandatory to make this plugin activated,
- `username` - Username if remote url required authentication. (Optional)
- `password` - Password for basic authentication for the remote endpoint (Optional)

The above example resolved the artifact from:
`http://somesite.com/a/b/c.zip`
and deploys the artifact to the following repository path:
`local-repo/a/b/c.zip`
