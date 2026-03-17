run-collector:
	mvn -q -pl telemetry/collector spring-boot:run

run-kafka:
	docker compose up -d

stop-kafka:
	docker compose stop kafka

down-kafka:
	docker compose down -v

collector-json-test:
	bash hub-router/scripts/macos_linux/1-collector-json-tests.sh

collector-grpc-test:
	bash hub-router/scripts/macos_linux/2-collector-grpc-tests.sh

aggregator-test:
	bash hub-router/scripts/macos_linux/3-aggregatorr-tests.sh

analyzer-test:
	bash hub-router/scripts/macos_linux/4-analyzer-tests.sh

show-kafka-logs:
	docker logs -f kafka

show-kafka-topics:
	docker exec -it kafka kafka-topics --bootstrap-server kafka:29092 --list