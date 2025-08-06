# Driver Alert Backend - FastAPI

## Setup Instructions

1. **Activate Virtual Environment:**
   ```bash
   .\venv\Scripts\Activate.ps1
   ```

2. **Install Dependencies (already done):**
   ```bash
   pip install -r requirements.txt
   ```

3. **Start the Server:**
   ```bash
   python main.py
   ```

## API Endpoints

- **Health Check:** `GET /api/health`
- **Authentication:** `POST /api/auth/login`
- **Active Drivers:** `GET /api/drivers/active`
- **Update Login:** `PUT /api/drivers/last-login`

## Database

Connects to MongoDB Atlas with the provided URI.
Database: `distract_alert`
Collection: `drivers`

## Mobile App Configuration

Update `BackendConfig.kt` in your Android app:
- **Android Emulator:** `http://10.0.2.2:8080`
- **Real Device:** `http://YOUR_COMPUTER_IP:8080`

## Running the Backend

```bash
cd driver-alert-backend
.\venv\Scripts\Activate.ps1
python main.py
```

The server will start on `http://localhost:8080`
