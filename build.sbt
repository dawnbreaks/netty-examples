name := "scala-netty-example"

version := "0.0.1"

scalaVersion := "2.11.6"

EclipseKeys.withSource:=true

resolvers += "rediscala" at "http://dl.bintray.com/etaty/maven"

libraryDependencies ++= Seq(
  "com.etaty.rediscala" %% "rediscala" 			% "1.4.0",
  "io.netty"            % "netty-all" 			% "4.0.21.Final",
  "com.google.guava"    % "guava" 				% "18.0",
  "ch.qos.logback"      % "logback-classic" 	% "1.1.2"
)
