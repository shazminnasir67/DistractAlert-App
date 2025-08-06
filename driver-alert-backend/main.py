from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from pymongo import MongoClient
import numpy as np
import json
import time
from typing import List, Optional

# FastAPI app instance
app = FastAPI(title="Driver Alert Backend", version="1.0.0")

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, specify your Android app's origin
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# MongoDB connection
MONGO_URI = "mongodb+srv://alviawan97:SMSzRZrR6nsUSwyV@cluster0.syz8muv.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0"
client = MongoClient(MONGO_URI)
db = client['distract_alert']
drivers_collection = db['drivers']

# Pydantic models
class Driver(BaseModel):
    _id: str
    user_id: str
    fleet_manager_id: str
    first_name: str
    last_name: str
    email: str
    phone: str
    license_number: str
    username: str
    account_status: str
    face_embedding_url: str
    created_at: int
    updated_at: int
    last_login: Optional[int] = None

class AuthenticationRequest(BaseModel):
    face_embedding: List[float]

class AuthenticationResponse(BaseModel):
    success: bool
    driver: Optional[Driver] = None
    confidence: Optional[float] = None
    message: str

class DriversResponse(BaseModel):
    success: bool
    data: List[Driver]
    message: Optional[str] = None

class UpdateLoginRequest(BaseModel):
    user_id: str

class UpdateLoginResponse(BaseModel):
    success: bool
    data: bool
    message: Optional[str] = None

class HealthResponse(BaseModel):
    success: bool
    data: str
    timestamp: int

def normalize_timestamp_field(value) -> int:
    """Normalize various timestamp formats to Unix timestamp in milliseconds"""
    if value is None:
        return int(time.time() * 1000)
    
    # If already an integer (Unix timestamp)
    if isinstance(value, int):
        # Check if it's in seconds (convert to milliseconds)
        if value < 10000000000:  # Less than year 2286 in seconds
            return value * 1000
        return value
    
    # If it's a string (ISO format)
    if isinstance(value, str):
        try:
            from datetime import datetime
            # Handle ISO format with timezone
            dt = datetime.fromisoformat(value.replace('Z', '+00:00'))
            return int(dt.timestamp() * 1000)
        except:
            return int(time.time() * 1000)
    
    # If it's a datetime object
    if hasattr(value, 'timestamp'):
        return int(value.timestamp() * 1000)
    
    # Fallback to current time
    return int(time.time() * 1000)

# Face recognition utility
def calculate_cosine_similarity(embedding1: List[float], embedding2: List[float]) -> float:
    """Calculate cosine similarity between two embeddings"""
    if len(embedding1) != len(embedding2):
        return 0.0
    
    embedding1_np = np.array(embedding1)
    embedding2_np = np.array(embedding2)
    
    dot_product = np.dot(embedding1_np, embedding2_np)
    norm1 = np.linalg.norm(embedding1_np)
    norm2 = np.linalg.norm(embedding2_np)
    
    if norm1 == 0 or norm2 == 0:
        return 0.0
    
    return float(dot_product / (norm1 * norm2))

# API endpoints
@app.get("/api/health", response_model=HealthResponse)
async def health_check():
    """Health check endpoint"""
    return HealthResponse(
        success=True,
        data="Backend service is running",
        timestamp=int(time.time() * 1000)
    )

@app.post("/api/auth/login", response_model=AuthenticationResponse)
async def authenticate_driver(request: AuthenticationRequest):
    """Authenticate driver using face embedding"""
    try:
        # Get all active drivers
        active_drivers = list(drivers_collection.find({'account_status': 'Active'}))
        
        best_match = None
        best_similarity = 0.0
        threshold = 0.85
        
        for driver_doc in active_drivers:
            try:
                # Parse face embedding from database
                driver_embedding = json.loads(driver_doc['face_embedding_url'])
                similarity = calculate_cosine_similarity(request.face_embedding, driver_embedding)
                
                if similarity > best_similarity and similarity >= threshold:
                    best_similarity = similarity
                    best_match = driver_doc
            except (json.JSONDecodeError, KeyError) as e:
                print(f"Error parsing embedding for driver {driver_doc.get('user_id', 'unknown')}: {e}")
                continue
        
        if best_match:
            # Update last login
            drivers_collection.update_one(
                {'user_id': best_match['user_id']},
                {
                    '$set': {
                        'last_login': int(time.time() * 1000),
                        'updated_at': int(time.time() * 1000)
                    }
                }
            )
            
            # Convert ObjectId to string for JSON serialization
            best_match['_id'] = str(best_match['_id'])
            
            # Normalize all timestamp fields
            for field in ['created_at', 'updated_at', 'last_login']:
                if field in best_match:
                    best_match[field] = normalize_timestamp_field(best_match[field])
            
            driver = Driver(**best_match)
            
            return AuthenticationResponse(
                success=True,
                driver=driver,
                confidence=best_similarity,
                message="Authentication successful"
            )
        else:
            raise HTTPException(
                status_code=401,
                detail="No matching driver found"
            )
    
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Authentication failed: {str(e)}"
        )

@app.get("/api/drivers/active", response_model=DriversResponse)
async def get_active_drivers():
    """Get all active drivers"""
    try:
        drivers = list(drivers_collection.find({'account_status': 'Active'}))
        
        # Convert ObjectId to string for each driver
        driver_list = []
        for driver_doc in drivers:
            driver_doc['_id'] = str(driver_doc['_id'])
            
            # Normalize all timestamp fields
            for field in ['created_at', 'updated_at', 'last_login']:
                if field in driver_doc:
                    driver_doc[field] = normalize_timestamp_field(driver_doc[field])
            
            driver_list.append(Driver(**driver_doc))
        
        return DriversResponse(
            success=True,
            data=driver_list
        )
    
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Failed to fetch active drivers: {str(e)}"
        )

@app.put("/api/drivers/last-login", response_model=UpdateLoginResponse)
async def update_last_login(request: UpdateLoginRequest):
    """Update last login time for a driver"""
    try:
        result = drivers_collection.update_one(
            {'user_id': request.user_id},
            {
                '$set': {
                    'last_login': int(time.time() * 1000),
                    'updated_at': int(time.time() * 1000)
                }
            }
        )
        
        success = result.modified_count > 0
        
        return UpdateLoginResponse(
            success=success,
            data=success
        )
    
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Failed to update last login: {str(e)}"
        )

# Root endpoint
@app.get("/")
async def root():
    return {"message": "Driver Alert Backend API", "version": "1.0.0", "status": "running"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8080)
