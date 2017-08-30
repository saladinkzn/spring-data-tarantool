# spring-data-tarantool
[![Build Status](https://travis-ci.org/saladinkzn/spring-data-tarantool.svg?branch=master)](https://travis-ci.org/saladinkzn/spring-data-tarantool)
[![Coverage Status](https://coveralls.io/repos/github/saladinkzn/spring-data-tarantool/badge.svg?branch=space_name)](https://coveralls.io/github/saladinkzn/spring-data-tarantool?branch=space_name)
![Maintenance Status](http://img.shields.io/maintenance/yes/2017.svg)
[![License](http://img.shields.io/badge/license-MIT-47b31f.svg)](#copyright-and-license)

Spring data repositories support for [Tarantool](https://tarantool.org)

Usage:

Add following snippet to your project:
```groovy
repositories {
  jcenter()
}

dependencies {
  compile 'ru.shadam:spring-data-tarantool:0.3.0'
}
```

and enable tarantool repositories:

```java
@EnableTarantoolRepositories 
public class ApplicationConfiguration {
    @Bean
    public TarantoolClientOps<Integer, List<?>, Object, List<?>> tarantoolSyncOps(
        TarantoolClient tarantoolClient
    ) {
        return tarantoolClient.syncOps();
    }

    @Bean(destroyMethod = "close")
    public TarantoolClient tarantoolClient(
        SocketChannelProvider socketChannelProvider,
        TarantoolClientConfig config
    ) {
        return new TarantoolClientImpl(socketChannelProvider, config);
    }

    @Bean
    public TarantoolClientConfig tarantoolClientConfig() {
        final TarantoolClientConfig config = new TarantoolClientConfig();
        config.username = "guest";
        return config;
    }

    @Bean
    public SocketChannelProvider socketChannelProvider() {
        return new SimpleSocketChannelProvider("localhost", 3301);
    }
}
```

#### Copyright and License

Copyright 2017 (c) Timur Shakurov.

All versions, present and past, of spring-data-tarantool are licensed under [MIT license](LICENSE).