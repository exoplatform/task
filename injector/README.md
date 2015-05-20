# task Injector

## Overview

Data injector is used to create data for performance test benchmark. This page will describe which Data Injectors are implemented in eXo Task Management and how to use them.

## Technical details

In eXo Task, data injectors are implemented as plugins attached to a service and handled via RESTful requests. The service is named "org.exoplatform.services.bench.DataInjectorService" and registered normally to portal container as general component.

Source: `https://github.com/exo-addons/task/tree/master/injector`
Download: `https://repository.exoplatform.org/service/local/artifact/maven/redirect?r=exo-addons-snapshots&g=org.exoplatform.addons.task&a=task-management-injector&v=1.0.x-SNAPSHOT`

## How to use

Users use RESTful service to request to inject or reject data. The format of request link is:
```
http://{domain}/{rest}/private/bench/{inject|reject}/{type}?\[params\]
```

* Example for standalone:
```
http://localhost:8080/rest/private/bench/inject/{type}?\[params\]
```

### Inject Tasks

#### Parameters

Each parameter is optional because they all have a default value.

| Parameter | Description | Default |
| --- | --- | --- |
| nbProject | Number of project per user/space | 15 |
|nbTaskPerProject|Number of tasks in a project|42 |
|nbIncomingTask|Number of tasks without project per user|10 |
|nbTagPerTask|Number of tags per task|2 |
|nbComPerTask|Number of comments per task|2 |
|perCompleted|Percentage of tasks completed|70 |
|type|Type of injecting: "user" or "space"|"user" |
|from|The begin of the range|0 |
|to|The end of the range|10 |
|prefix|Define the user prefix|"bench.space" if type = "space", "bench.user" otherwise |
|suffix|Define the user suffix (How many digit after prefix)|4 |

#### Sample

##### Default for user tasks

Generate for 10 users (from bench.user0000 to bench.user0009) 15 projects with 42 tasks in + 10 incoming tasks (tasks without project). Each tasks have 2 tags and 2 comments. 70% of tasks are completed.
```
http://localhost:8080/rest/private/bench/inject/PersonnalTaskInjector
```

##### Specific for user tasks

Generate for 10 users (from **abcuser000010** to **abcuser000019**) 15 projects with 42 tasks in + 10 incoming tasks (tasks without project). Each tasks have 2 tags and 2 comments. 70% of tasks are completed.
```
http://localhost:8080/rest/private/bench/inject/PersonnalTaskInjector?prefix=abcuser&suffix=6&from=10&to=20
```

Generate for 10 users (from bench.user0000 to bench.user0009) **30 projects** with **10 tasks** in + **5 incoming tasks** (tasks without project). Each tasks have **3 tags** and **15 comments**. 70% of tasks are completed.
```
http://localhost:8080/rest/private/bench/inject/PersonnalTaskInjector?nbProject=30&nbTaskPerProject=10&nbIncomingTask=5&nbTagPerTask=3&nbComPerTask=15
```

Generate for 10 users (from bench.user0000 to bench.user0009) 15 projects with 42 tasks in + 10 incoming tasks (tasks without project). Each tasks have 2 tags and 2 comments. **0% of tasks are completed**.
```
http://localhost:8080/rest/private/bench/inject/PersonnalTaskInjector?perCompleted=0
```

##### Default for space tasks

Generate for **10 space (from benchspace0000 to benchspace0009)** 15 projects with 42 tasks in. Each tasks have 2 tags and 2 comments. 70% of tasks are completed.
```
http://localhost:8080/rest/private/bench/inject/PersonnalTaskInjector?type=space
```