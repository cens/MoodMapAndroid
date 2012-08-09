/*******************************************************************************
 * Copyright 2012 Intel-GE Care Innovations(TM)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.mood.models;

public class Mood
{
    /** X axis to draw */
    public float X = -50f;
    /** Y axis to draw */
    public float Y = -50f;
    /** Checking flag while drag the mood */
    public boolean isSelected = false;
    /** Checking flag while Inserting and Posting the mood */
    public boolean isCreated = false;
    /** X Possition on 10/10 graph */
    public float moodLevel = -50;
    /** Y Possition on 10/10 graph */
    public float energyLevel = -50;
}
