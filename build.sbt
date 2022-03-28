name := "scala-akka-grpc"

version in ThisBuild := "0.0.1"

scalaVersion := "2.13.8"

val akkaVersion     = "2.6.14"
val akkaHttpVersion = "10.2.6"
val akkaGrpcVersion = "1.38.0"

//val scalapb = Seq(
//  "com.thesamet.scalapb" %% "scalapb-runtime" % "0.11.6"
//)

val akkaStack = Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream"      % akkaVersion,
  "com.typesafe.akka" %% "akka-discovery"   % akkaVersion,
  "com.typesafe.akka" %% "akka-pki"         % akkaVersion
)

val akkaHttpStack = Seq(
  "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http2-support"   % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
)

val circeStack                     = Seq(
  "circe-core",
  "circe-generic",
  "circe-parser",
  "circe-literal",
  "circe-generic-extras"
).map("io.circe" %% _ % "0.13.0")

//目前只支持到akka-http 10.2.6 ???
val akkaHttpJsonSerializersVersion = "1.38.2"
val jackson                        = Seq(
  "de.heikoseeberger" %% "akka-http-jackson" % akkaHttpJsonSerializersVersion
)

//val akkaGrpcStakc = Seq(
//  "io.grpc" % "grpc-core" % akkaGrpcVersion,
//  "io.grpc" % "grpc-netty-shaded" % akkaGrpcVersion
//)

val grpcWebStack = Seq(
  "ch.megard" %% "akka-http-cors" % "0.4.2"
)

val jwtStack = Seq(
  "com.pauldijou" %% "jwt-core" % "4.3.0"
)

val loggingStack = Seq(
  "ch.qos.logback"           % "logback-classic"        % "1.2.3",
  "org.slf4j"                % "slf4j-api"              % "1.7.30",
  "com.github.danielwegener" % "logback-kafka-appender" % "0.2.0-RC2"
)

val googleGuiceStack = Seq(
  "net.codingwell" %% "scala-guice" % "5.0.0"
)

libraryDependencies in ThisBuild ++=
//  scalapb ++
  akkaStack ++
    akkaHttpStack ++
//    akkaGrpcStakc ++
    loggingStack ++
    googleGuiceStack ++
    grpcWebStack

val schemas = Project(id = "schemas", base = file("schemas"))
  .enablePlugins(JavaAppPackaging, AkkaGrpcPlugin)
  .dependsOn()
  .settings(
  )

val common = Project(id = "common", base = file("common"))
  .enablePlugins(JavaAppPackaging, AkkaGrpcPlugin)
  .dependsOn()
  .settings(
  )

val grpcServer = Project(id = "grpc-server", base = file("grpc-server"))
  .enablePlugins(JavaAppPackaging, AkkaGrpcPlugin)
  .dependsOn(schemas, common)
  .settings(libraryDependencies ++= Seq.empty)

val grpcClient = Project(id = "grpc-client", base = file("grpc-client"))
  .enablePlugins(JavaAppPackaging, AkkaGrpcPlugin)
  .dependsOn(schemas, common)
  .settings(
    libraryDependencies ++=
      circeStack
        ++ jackson
  )
