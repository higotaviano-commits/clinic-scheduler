# 📊 JaCoCo Code Coverage Report

## Como Acessar o Relatório

O relatório de cobertura de testes foi gerado automaticamente após a execução dos testes unitários.

### 📍 Localização do Relatório
```
target/site/jacoco/index.html
```

### 🌐 Abrindo o Relatório

#### No Windows Explorer
1. Navegue até: `C:\Users\higoo\Desktop\desafio_fiap_fase_3\clinic-scheduler\agendamento-service\target\site\jacoco\`
2. Clique duplo em `index.html`
3. O relatório será aberto no seu navegador padrão

#### Via Linha de Comando
```bash
# Windows (PowerShell)
start .\target\site\jacoco\index.html

# Windows (CMD)
start target\site\jacoco\index.html

# Linux/Mac
open target/site/jacoco/index.html  # Mac
xdg-open target/site/jacoco/index.html  # Linux
```

## 📈 Interpretando o Relatório

O relatório mostra:

- **Line Coverage (Cobertura de Linhas)**: % de linhas executadas pelos testes
- **Branch Coverage (Cobertura de Ramificações)**: % de decisões tomadas (if/else, loops, etc.)
- **Complexity (Complexidade)**: Complexidade ciclomática do código

### 🎯 Métrica Alvo
```
Cobertura Mínima: 50% (configurado no pom.xml)
Atual: Verificar no relatório
```

## 📊 Estatísticas de Testes

### Total de Testes: **80** ✅
- ✅ Testes de Serviço: 55
- ✅ Testes Unitários (Filter/Exception): 24
- ✅ Taxa de Sucesso: 100%
- ✅ Falhas: 0
- ✅ Erros: 0

### Classes Testadas

#### Services
- `AuthService` - 8 testes
- `UsuarioService` - 9 testes
- `ConsultaService` - 20 testes
- `JwtService` - 18 testes

#### Unit Tests
- `JwtFilter` - 9 testes
- `GlobalExceptionHandler` - 15 testes

## 🔧 Regenerando o Relatório

Para regenerar o relatório de cobertura, execute:

```bash
mvn clean test -DskipITs
```

O novo relatório será gerado em `target/site/jacoco/index.html`

## 📝 Estrutura do Relatório

```
target/site/jacoco/
├── index.html                          # Página principal
├── br.com.fiap.techchallenge.agendamento/
│   ├── service/
│   │   ├── AuthService.html
│   │   ├── ConsultaService.html
│   │   ├── JwtService.html
│   │   └── UsuarioService.html
│   ├── filter/
│   │   └── JwtFilter.html
│   ├── exception/
│   │   └── GlobalExceptionHandler.html
│   ├── controller/
│   ├── model/
│   └── ... (outros packages)
```

## 🎯 Recomendações

1. **Manter acima de 50%**: Cobertura mínima configurada
2. **Aumentar para 70-80%**: Ideal para produção
3. **Focar em lógica de negócio**: Services e Filters
4. **Controllers**: Podem ter cobertura menor (requerem testes de integração)

## 📚 Mais Informações

- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [JaCoCo Maven Plugin](https://www.jacoco.org/jacoco/trunk/doc/maven.html)

---

**Último gerado**: Agora (após `mvn clean test`)
