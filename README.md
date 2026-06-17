# PomoHome

### Iniciar Servidor
```bash
# No diretório principal ./PomoHome/
./gradlew :backend:bootRun
```

### Iniciar Jogo

```bash
# No diretório principal ./PomoHome/
./gradlew :lwjgl3:run
```

### Adicionar 10 Móveis na Loja do servidor

```bash
# Servidor precisa estar rodando (porta 8080)
add() { curl -s -X POST http://localhost:8080/api/loja -H "Content-Type: application/json" -d "$1"; echo; }
add '{"nome":"Cama de Casal","categoria":"cama","preco":200}'
add '{"nome":"Sofa Retratil","categoria":"sofa","preco":140}'
add '{"nome":"Mesa de Jantar","categoria":"mesa","preco":160}'
add '{"nome":"Cadeira Gamer","categoria":"cadeira","preco":110}'
add '{"nome":"Tapete Persa","categoria":"tapete","preco":70}'
add '{"nome":"Planta Suculenta","categoria":"planta","preco":40}'
add '{"nome":"Estante de Livros","categoria":"mesa","preco":130}'
add '{"nome":"Poltrona","categoria":"cadeira","preco":95}'
add '{"nome":"Cama Beliche","categoria":"cama","preco":180}'
add '{"nome":"Vaso Decorativo","categoria":"planta","preco":35}'
```

### Adicionar dinheiro na conta de jogador

```bash
# Por id (ex.: jogador 1 recebe 5000 moedas)
curl -s -X POST "http://localhost:8080/api/jogadores/1/creditar?valor=5000"

# Descobrir o id pelo username
curl -s http://localhost:8080/api/jogadores/username/SEU_USERNAME
```