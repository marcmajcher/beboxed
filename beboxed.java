int box_size    = 22;
int box_spacing = 2;
int grid_size   = 8;
int grid_offset = 5;
int num_colors  = 7;

int gravity  = 1;
int ground_y = 196;
boolean gravity_on = true;
boolean accept_clicks = false;

Box current_selection = null;
Box[][] grid = new Box[grid_size][grid_size];
color[] colors = new color[num_colors];
color bgcolor = color(200,200,200);

void setup()
{
  size(201,201);
  framerate(24);
  
  colors[0] = color(200,0,0);
  colors[1] = color(0,200,0);
  colors[2] = color(0,0,200);
  colors[3] = color(200,120,0);
  colors[4] = color(120,0,200);
  colors[5] = color(200,200,0);
  colors[6] = color(255,255,255);

  // pre-initialize grid
  for (int y=0; y<grid_size; y++) {
    for (int x=0; x<grid_size; x++) {
      grid[x][y] = new Box(-1, x, y);
    }
  }

  // set grid colors, checking for match3
  for (int y=0; y<grid_size; y++) {
    for (int x=0; x<grid_size; x++) {
      
      int box_color = int(random(num_colors));
      grid[x][y] = new Box(box_color, x, y);
      
      while (isMatchThree(x, y)) {
        box_color = int(random(num_colors));
        grid[x][y] = new Box(box_color, x, y);
      }    

      grid[x][y].placeBox(200 + int(random(100)));
    } 
  }

}

void draw()
{
  background(bgcolor);
  
  for (int y=grid_size-1; y>=0; y--) {
    for (int x=0; x<grid_size; x++) {
      Box mybox = grid[x][y];
      
      accept_clicks = true;
      
      // animate with physics
      if (gravity_on) {
        mybox.speed_y += gravity;
        int new_y = mybox.box_y + mybox.speed_y;

        // check for collision with ground
        if (new_y + box_size + box_spacing > ground_y) {
          new_y = ground_y - box_size - box_spacing;  
        }
      
        // check for collisions with other boxes in column
        for (int i=y+1; i<grid_size; i++) {
           if (new_y + box_size + box_spacing > grid[x][i].box_y) {
             new_y = grid[x][i].box_y - box_size - box_spacing;
           }
        }
      
        // keep clicks off while we're animating
        if (new_y != mybox.box_y) {
          mybox.box_y = new_y;
          accept_clicks = false;
        }
      }
      
      // check if mouse cursor is over a box
      if (mybox.pointInsideBox(mouseX, mouseY) && accept_clicks) {
        mybox.hovered = true; 
      }
      else {
        mybox.hovered = false;
      }
      
      // check to see if any match3 has fallen into place
      Box[] matches = mybox.getMatches();
      if (matches != null) {
        for (int i=0; i<matches.length-1; i++) {
          //if (matches[i] != null) {
            matches[i].removeBox();
          //}
        }            
      }
      
      // finally, draw!
      mybox.drawBox();
    } 
  }
  
  
}

void mousePressed()
{
  if (!accept_clicks) {
    return;
  }
  
  Box swap1 = null;
  Box swap2 = null;
  Box tmp_selection = null;
  
  for (int y=0; y<grid_size; y++) {
    for (int x=0; x<grid_size; x++) {
      Box mybox = grid[x][y];
            
      // check if mouse cursor is over a box
      if (mybox.pointInsideBox(mouseX, mouseY)) {
       
        if (mybox.selected) {
          mybox.selected = false;
        }
        else {
          if (isAdjacent(mybox, current_selection)) {
              swap1 = mybox;
              swap2 = current_selection;
              mybox.selected = false;
          }
          else {
            mybox.selected = true;
            tmp_selection = mybox;
          }
        }

      }
      else {
        mybox.selected = false;
      }
    }
  }
  
  if (swap1 != null && swap2 != null) {
    trySwap(swap1, swap2);
  }
  
  current_selection = tmp_selection;

  if (current_selection != null) {
    //println("Current selection: "+current_selection.grid_x+","+current_selection.grid_y);
  }

}

// Game Logic

boolean isAdjacent(Box b1, Box b2)
{
  if (b1 == null || b2 == null) {
    return false;
  }
  
  if (b1.grid_x == b2.grid_x) {
    if (b1.grid_y == b2.grid_y+1 ||
        b1.grid_y == b2.grid_y-1) {
      return true;    
    }
  };
  if (b1.grid_y == b2.grid_y) {
    if (b1.grid_x == b2.grid_x+1 ||
        b1.grid_x == b2.grid_x-1) {
      return true;    
    }
  }
  
  return false;
}

boolean isMatchThree(int x, int y) 
{
  if (grid[x][y].getMatches() == null) {
    return false;
  }
  else {
    return true;
  }
}

void trySwap(Box swap1, Box swap2)
{
  // swap boxes in grid
  swapBoxes(swap1, swap2);

  // check for matches
  Box[] matches1 = swap1.getMatches();
  Box[] matches2 = swap2.getMatches();

  // if there's no match, swap 'em back
  if (matches1 == null && matches2 == null) {
    swapBoxes(swap1, swap2);
  }

  // remove matches
  if (matches1 != null) {
    for (int i=0; i<matches1.length-1; i++) {
      if (matches1[i] != null) {
        matches1[i].removeBox();
      }
    }    
  }
  if (matches2 != null) {
    for (int i=0; i<matches2.length-1; i++) {
      if (matches2[i] != null) {
        matches2[i].removeBox();
      }
    }
  }
      
}

void swapBoxes(Box swap1, Box swap2) 
{
  int gx1 = swap1.grid_x;
  int gy1 = swap1.grid_y;
  int gx2 = swap2.grid_x;
  int gy2 = swap2.grid_y;
    
  grid[gx1][gy1] = swap2;
  grid[gx2][gy2] = swap1;
  
  grid[gx1][gy1].grid_x = gx1;
  grid[gx1][gy1].grid_y = gy1;
  grid[gx2][gy2].grid_x = gx2;
  grid[gx2][gy2].grid_y = gy2;

  // do move - TBD animate
  int tmpx = swap1.box_x;
  int tmpy = swap1.box_y;
  swap1.box_x = swap2.box_x;
  swap1.box_y = swap2.box_y;
  swap2.box_x = tmpx;
  swap2.box_y = tmpy;  
}

void printGrid()
{
  for (int y=0; y<grid_size; y++) {
    for (int x=0; x<grid_size; x++) {
      print(grid[x][y].box_color+" ");
    }
    println();
  }  
}


//  Box class

class Box 
{
  int box_color;
  int box_x;
  int box_y;
  int grid_x;
  int grid_y;
  int speed_y;
  boolean selected;
  boolean hovered;
  
  Box(int c, int x, int y)
  {
    box_color = c;
    grid_x = x;
    grid_y = y;
    box_x = -999;
    box_y = -999;
    selected = false;
    hovered = false;
    speed_y = 0;
  }
  
  void drawBox()
  {
    fill(colors[box_color]);

    if (selected) {
      stroke(255,0,0);
    } 
    else {
      if (hovered) {
        stroke(0,0,0);
      }
      else {
        stroke(bgcolor);
      }
    }

    rect(box_x, box_y, box_size, box_size);    
  }
  
  boolean pointInsideBox(int x, int y)
  {
    if (x >= box_x && x < box_x+box_size &&
        y >= box_y && y < box_y+box_size) {
      return(true);     
    } 
    else {
      return(false);  
    }
  }
  
  Box[] getMatches() 
  {
    int match_up    = 0;
    int match_down  = 0;
    int match_left  = 0;
    int match_right = 0;
    
    // ahh, the hackery that java forces...
    int[] match_hx = {};
    int[] match_hy = {};
    int[] match_vx = {};
    int[] match_vy = {};
    
    // check squares to the right
    for (int cx=grid_x+1; cx<grid_size; cx++) {
      if (box_color == grid[cx][grid_y].box_color) {
        match_right++;
        match_hx = append(match_hx, cx);
        match_hy = append(match_hy, grid_y);
      }
      else {
        break;
      }
    }  

    // check squares to the left
    for (int cx=grid_x-1; cx>=0; cx--) {
      if (box_color == grid[cx][grid_y].box_color) {
        match_left++;
        match_hx = append(match_hx, cx);
        match_hy = append(match_hy, grid_y);
      }
      else {
        break;
      }
    }  

    // check squares below
    for (int cy=grid_y+1; cy<grid_size; cy++) {
      if (box_color == grid[grid_x][cy].box_color) {
        match_down++;
        match_vx = append(match_vx, grid_x);
        match_vy = append(match_vy, cy);
      }
      else {
        break;
      }
    }  

    // check squares above
    for (int cy=grid_y-1; cy>=0; cy--) {
      if (box_color == grid[grid_x][cy].box_color) {
        match_up++;
        match_vx = append(match_vx, grid_x);
        match_vy = append(match_vy, cy);
      }
      else {
        break;
      }
    }  

    // check for a horizontal or vertical match3
    int match_h = match_left + 1 + match_right;
    int match_v = match_up + 1 + match_down;

    if (match_h >= 3 || match_v >= 3) {
      int match_count = 1;
    
      if (match_h >= 3) {
        match_count += match_h;
      }
      if (match_v >= 3) {
        match_count += match_v;
      }

      Box[] matches = new Box[match_count];
      matches[0] = this;
      match_count = 1;
    
      if (match_h >= 3) {
        for (int i=0; i<match_hx.length; i++) {
          matches[match_count] = grid[match_hx[i]][match_hy[i]];
          match_count++;
        }
      }
      if (match_v >= 3) {
        for (int i=0; i<match_vx.length; i++) {
          matches[match_count] = grid[match_vx[i]][match_vy[i]];
          match_count++;
        }        
      }
      
      return matches;
    }

    return(null);
  }
  
  void removeBox()
  {
    //println("Removing box: "+grid_x+","+grid_y);
    for (int i=grid_y; i>0; i--) {
      grid[grid_x][i] = grid[grid_x][i-1];
      grid[grid_x][i].grid_y++; 
    }
    grid[grid_x][0] = new Box(int(random(num_colors)), grid_x, 0);
    grid[grid_x][0].placeBox(box_size + int(random(40)));

  }
  
  void placeBox(int offset)
  {
    box_x = grid_x * (box_size + box_spacing) + grid_offset;
    box_y = grid_y * (box_size + box_spacing) + grid_offset;
    // raise up for the fall
    box_y -= offset;
  }
}
2 min to Spreed
