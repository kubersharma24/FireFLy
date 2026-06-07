# 📧 Email Agent — Spring Boot + Java 21 + Gradle

An AI-powered email agent that:
1. Receives an email generation request via REST API
2. Sends it to **Anthropic Claude** (LLM) to generate a professional email template
3. Dispatches the generated email to the recipient via **SMTP**

---

## 🏗️ Architecture

```
POST /api/v1/email/send
        │
        ▼
┌─────────────────────────┐
│   EmailController        │  ← REST layer (validation + routing)
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│  EmailOrchestratorAgent  │  ← Coordinates the two agents below
└────────┬────────┬────────┘
         │        │
         ▼        ▼
┌──────────────┐  ┌──────────────────┐
│ LlmEmail     │  │ EmailSender      │
│ GeneratorAgent│  │ Agent            │
│ (Claude API) │  │ (SMTP/JavaMail)  │
└──────────────┘  └──────────────────┘
```

---

## 🚀 Quick Start

### Prerequisites
- Java 21
- Gradle 8+
- Anthropic API key
- Gmail account with App Password (or any SMTP server)

### 1. Clone & Configure

Set the required environment variables:

```bash
export ANTHROPIC_API_KEY=sk-ant-...
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-gmail-app-password    # Gmail App Password, NOT your login password
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
```

Or edit `src/main/resources/application.properties` directly.

> **Gmail App Password**: Go to Google Account → Security → 2-Step Verification → App Passwords → Generate one for "Mail".

### 2. Build & Run

```bash
cd email-agent
./gradlew bootRun
```

Or build a jar:
```bash
./gradlew build
java -jar build/libs/email-agent-0.0.1-SNAPSHOT.jar
```

---

## 📬 API Usage

### Send Email

**POST** `http://localhost:8080/api/v1/email/send`

```json
{
  "toEmail": "lala@gmail.com",
  "Description": "draft me a mail template to send a congratulation email to my employee over his achievement in office and well done for that",
  "topic": "congratulation email to employee",
  "my number": "6878367864",
  "Myname": "Kuber",
  "ToName": "Lala"
}
```

**Success Response (200)**:
```json
{
  "success": true,
  "message": "Email generated and sent successfully!",
  "generatedSubject": "Congratulations on Your Outstanding Achievement, Lala!",
  "generatedBody": "Dear Lala,\n\nI wanted to take a moment...",
  "sentTo": "lala@gmail.com",
  "error": null
}
```

**Failure Response (500)**:
```json
{
  "success": false,
  "message": "Email sending failed",
  "error": "SMTP connection failed: ...",
  ...
}
```

### Health Check

**GET** `http://localhost:8080/api/v1/email/health`

```
Email Agent is running ✅
```

---

## 🧪 Run Tests

```bash
./gradlew test
```

---

## 📁 Project Structure

```
email-agent/
├── build.gradle
├── settings.gradle
├── README.md
└── src/
    ├── main/
    │   ├── java/com/emailagent/
    │   │   ├── EmailAgentApplication.java          # Spring Boot entry point
    │   │   ├── controller/
    │   │   │   └── EmailController.java            # REST endpoints
    │   │   ├── agent/
    │   │   │   ├── EmailOrchestratorAgent.java     # Main coordinator agent
    │   │   │   ├── LlmEmailGeneratorAgent.java     # Calls Anthropic Claude API
    │   │   │   └── EmailSenderAgent.java           # SMTP email dispatcher
    │   │   ├── model/
    │   │   │   ├── EmailRequest.java               # Incoming request model
    │   │   │   ├── EmailResponse.java              # API response model
    │   │   │   └── GeneratedEmailContent.java      # LLM output model
    │   │   └── config/
    │   │       ├── AnthropicConfig.java            # Anthropic API config + OkHttpClient bean
    │   │       └── GlobalExceptionHandler.java     # Centralised error handling
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/emailagent/agent/
            └── EmailOrchestratorAgentTest.java
```

---

## 🔧 Configuration Reference

| Property | Env Var | Description |
|---|---|---|
| `anthropic.api.key` | `ANTHROPIC_API_KEY` | Your Anthropic API key |
| `anthropic.model` | — | Claude model (default: claude-3-5-sonnet-20241022) |
| `spring.mail.username` | `MAIL_USERNAME` | Sender email address |
| `spring.mail.password` | `MAIL_PASSWORD` | SMTP password / App password |
| `spring.mail.host` | `MAIL_HOST` | SMTP host (default: smtp.gmail.com) |
| `spring.mail.port` | `MAIL_PORT` | SMTP port (default: 587) |

---

## 🛡️ Notes

- Uses **OkHttp** for direct Anthropic API calls (no Spring AI dependency needed for MVP)
- Email body is sent as **HTML** for clean rendering in email clients
- **Validation** is applied on all required fields — bad requests get a 400 with clear messages
- Logs clearly annotated with `[Orchestrator]`, `[LLM Agent]`, `[Email Sender Agent]` for easy tracing
