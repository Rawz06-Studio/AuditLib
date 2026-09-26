# SCHEMA.md — Audit Log Format Specification

## Version 1.0

Template exact du format de sortie audit :

```
AUDIT|<full.class.name>#<methodName>|<arg1=val1, arg2=val2, ...>|<STATUS>|<duration_ms>
```

### Composants

| Champ | Type | Description |
|-------|------|-------------|
| `AUDIT` | Littéral | Identifiant fixe pour tous les logs d'audit |
| `full.class.name` | String | Nom complet de la classe avec package |
| `methodName` | String | Nom de la méthode auditée |
| `arg1=val1, ...` | String | Arguments sérialisés. Format `paramName=value` (si compilation avec `-parameters`) ou `argN=value`. Séparé par `, ` |
| `STATUS` | Enum | `SUCCESS` ou `ERROR` |
| `duration_ms` | Integer | Durée d'exécution en millisecondes, ou `-1` si calcul échoué |

### Séparateur

Le pipe `|` est le seul séparateur utilisé. Les valeurs ne contiennent jamais de pipe.

### Exemples

#### Succès
```
AUDIT|com.example.Service#transferFunds|accountId=1234, amount=5000|SUCCESS|45
```

#### Erreur
```
AUDIT|com.example.Auth#login|username=john, password=***|ERROR|12
```

#### Aucun argument
```
AUDIT|com.example.Util#getCurrentTime||SUCCESS|2
```

#### Masquage appliqué
```
AUDIT|com.example.Payment#checkout|cardNumber=****1234, cvv=***|SUCCESS|156
```

### Masquage (mask)

- Les patterns regex s'appliquent sur le **nom du paramètre**
- Une correspondance remplace la valeur par `***`
- Les patterns invalides (regex cassée) sont ignorés silencieusement

**Exemple :**
```java
@Audited(mask = {"password", "token", "secret.*"})
public void authenticate(String username, String password, String secretKey) { ... }
```

Résultat :
```
AUDIT|com.example.Auth#authenticate|username=admin, password=***, secretKey=***|SUCCESS|8
```

### Sérialisation d'arguments

- `null` → `"null"`
- Objet → `object.toString()`
- Valeur > 2000 caractères → tronquée avec `...`
- Erreur de `toString()` → `<toString_failed>`

### Durée

- Mesurée en **millisecondes**
- Nombre entier positif (jamais de suffixe `ms`)
- `-1` si le calcul échoue (exemple : exception en mesurant le temps)

### Sortie

- Destination : **System.out** (uniquement)
- Encodage : UTF-8
- Une ligne par invocation

### Erreurs internes d'audit

Les erreurs internes (serialization, duration calculation, log emission) sont émises sur **System.err** avec le préfixe `[AUDIT-INTERNAL]` :

```
[AUDIT-INTERNAL] Failed to serialize arguments: NullPointerException
```

Ces erreurs n'interrompent jamais l'exécution du code métier.

## Compatibilité

Changements qui modifient la version :

- Réordonner les champs → **MAJOR**
- Ajouter un nouveau champ optionnel (ex: `|MAJOR|`) → **MINOR**
- Changer le format d'un champ existant → **MAJOR**
- Changer le séparateur principal → **MAJOR**

Changements compatibles (patch ou invisible) :

- Valeurs internes du status (tant qu'on reste SUCCESS/ERROR) → patch
- Amélioration de sérialisation d'arguments (ex: meilleur toString()) → patch
