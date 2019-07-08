package featurecat.lizzie.gui;

import static java.awt.event.KeyEvent.*;

import featurecat.lizzie.Lizzie;
import java.awt.event.*;
import javax.swing.*;

public class Input implements MouseListener, KeyListener, MouseWheelListener, MouseMotionListener {
  @Override
  public void mouseClicked(MouseEvent e) {}

  @Override
  public void mousePressed(MouseEvent e) {
    if (e.getButton() == MouseEvent.BUTTON1) // left click
    Lizzie.frame.onClicked(e.getX(), e.getY());
    else if (e.getButton() == MouseEvent.BUTTON3) // right click
    undo();
  }

  @Override
  public void mouseReleased(MouseEvent e) {}

  @Override
  public void mouseEntered(MouseEvent e) {}

  @Override
  public void mouseExited(MouseEvent e) {}

  @Override
  public void mouseDragged(MouseEvent e) {
    Lizzie.frame.onMouseDragged(e.getX(), e.getY());
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    Lizzie.frame.onMouseMoved(e.getX(), e.getY());
  }

  @Override
  public void keyTyped(KeyEvent e) {}

  private void undo() {
    undo(1);
  }

  private void undo(int movesToAdvance) {
    if (Lizzie.board.inAnalysisMode()) Lizzie.board.toggleAnalysis();
    if (Lizzie.frame.isPlayingAgainstLeelaz) {
      Lizzie.frame.isPlayingAgainstLeelaz = false;
    }
    if (Lizzie.frame.incrementDisplayedBranchLength(-movesToAdvance)) {
      return;
    }

    for (int i = 0; i < movesToAdvance; i++) Lizzie.board.previousMove();
  }

  private void undoToChildOfPreviousWithVariation() {
    // Undo until the position just after the junction position.
    // If we are already on such a position, we go to
    // the junction position for convenience.
    // Use cases:
    // [Delete branch] Call this function and then deleteMove.
    // [Go to junction] Call this function twice.
    if (!Lizzie.board.undoToChildOfPreviousWithVariation()) Lizzie.board.previousMove();
  }

  private void undoToFirstParentWithVariations() {
    if (Lizzie.board.undoToChildOfPreviousWithVariation()) {
      Lizzie.board.previousMove();
    }
  }

  private void goCommentNode(boolean moveForward) {
    if (moveForward) {
      redo(Lizzie.board.getHistory().getCurrentHistoryNode().goToNextNodeWithComment());
    } else {
      undo(Lizzie.board.getHistory().getCurrentHistoryNode().goToPreviousNodeWithComment());
    }
  }

  private void redo() {
    redo(1);
  }

  private void redo(int movesToAdvance) {
    if (Lizzie.board.inAnalysisMode()) Lizzie.board.toggleAnalysis();
    if (Lizzie.frame.isPlayingAgainstLeelaz) {
      Lizzie.frame.isPlayingAgainstLeelaz = false;
    }
    if (Lizzie.frame.incrementDisplayedBranchLength(movesToAdvance)) {
      return;
    }

    for (int i = 0; i < movesToAdvance; i++) Lizzie.board.nextMove();
  }

  private void startRawBoard() {
    if (!Lizzie.config.showRawBoard) {
      Lizzie.frame.startRawBoard();
    }
    Lizzie.config.showRawBoard = true;
  }

  private void stopRawBoard() {
    Lizzie.frame.stopRawBoard();
    Lizzie.config.showRawBoard = false;
  }

  private void toggleHints() {
    Lizzie.config.toggleShowBranch();
    Lizzie.config.showSubBoard =
        Lizzie.config.showNextMoves = Lizzie.config.showBestMoves = Lizzie.config.showBranch;
  }

  private void nextBranch() {
    if (Lizzie.frame.isPlayingAgainstLeelaz) {
      Lizzie.frame.isPlayingAgainstLeelaz = false;
    }
    Lizzie.board.nextBranch();
  }

  private void previousBranch() {
    if (Lizzie.frame.isPlayingAgainstLeelaz) {
      Lizzie.frame.isPlayingAgainstLeelaz = false;
    }
    Lizzie.board.previousBranch();
  }

  private void moveBranchUp() {
    Lizzie.board.moveBranchUp();
  }

  private void moveBranchDown() {
    Lizzie.board.moveBranchDown();
  }

  private void deleteMove() {
    Lizzie.board.deleteMove();
  }

  private void deleteBranch() {
    Lizzie.board.deleteBranch();
  }

  private boolean controlIsPressed(KeyEvent e) {
    boolean mac = System.getProperty("os.name", "").toUpperCase().startsWith("MAC");
    return e.isControlDown() || (mac && e.isMetaDown());
  }

  private void toggleShowDynamicKomi() {
    Lizzie.config.showDynamicKomi = !Lizzie.config.showDynamicKomi;
  }

  @Override
  public void keyPressed(KeyEvent e) {
    // If any controls key is pressed, let's disable analysis mode.
    // This is probably the user attempting to exit analysis mode.
    boolean shouldDisableAnalysis = true;

    switch (e.getKeyCode()) {
      case VK_RIGHT:
        if (e.isShiftDown()) {
          moveBranchDown();
        } else {
          nextBranch();
        }
        break;

      case VK_LEFT:
        if (e.isShiftDown()) {
          moveBranchUp();
        } else if (controlIsPressed(e)) {
          undoToFirstParentWithVariations();
        } else {
          previousBranch();
        }
        break;

      case VK_UP:
        if (controlIsPressed(e) && e.isShiftDown()) {
          goCommentNode(false);
        } else if (e.isShiftDown()) {
          undoToChildOfPreviousWithVariation();
        } else if (controlIsPressed(e)) {
          undo(10);
        } else {
          undo();
        }
        break;

      case VK_PAGE_DOWN:
        if (controlIsPressed(e) && e.isShiftDown()) {
          Lizzie.frame.increaseMaxAlpha(-5);
        } else {
          redo(10);
        }
        break;

      case VK_DOWN:
        if (controlIsPressed(e) && e.isShiftDown()) {
          goCommentNode(true);
        } else if (controlIsPressed(e)) {
          redo(10);
        } else {
          redo();
        }
        break;

      case VK_N:
        if (controlIsPressed(e)) {
          // stop the ponder
          if (Lizzie.leelaz.isPondering()) Lizzie.leelaz.togglePonder();
          LizzieFrame.startNewGame();
        } else if (e.isShiftDown()) {
          Lizzie.leelaz.set_opponent("none");
          Lizzie.leelaz.opponent_display = "WHITE AND BLACK";
        }
        break;
      case VK_SPACE:
        if (Lizzie.frame.isPlayingAgainstLeelaz) {
          Lizzie.frame.isPlayingAgainstLeelaz = false;
          Lizzie.leelaz.isThinking = false;
        }
        Lizzie.leelaz.togglePonder();
        break;

      case VK_P:
        Lizzie.board.pass();
        break;

      case VK_COMMA:
        if (!Lizzie.frame.playCurrentVariation()) Lizzie.frame.playBestMove();
        break;

      case VK_M:
        Lizzie.config.toggleShowMoveNumber();
        break;

      case VK_F:
        Lizzie.config.toggleShowNextMoves();
        break;

      case VK_H:
        Lizzie.config.toggleHandicapInsteadOfWinrate();
        break;

      case VK_PAGE_UP:
        if (controlIsPressed(e) && e.isShiftDown()) {
          Lizzie.frame.increaseMaxAlpha(5);
        } else {
          undo(10);
        }
        break;

      case VK_I:
        // stop the ponder
        if (Lizzie.leelaz.isPondering()) Lizzie.leelaz.togglePonder();
        Lizzie.frame.editGameInfo();
        break;
      case VK_S:
        // stop the ponder
        if (Lizzie.leelaz.isPondering()) Lizzie.leelaz.togglePonder();
        LizzieFrame.saveFile();
        break;

      case VK_O:
        if (Lizzie.leelaz.isPondering()) Lizzie.leelaz.togglePonder();
        LizzieFrame.openFile();
        break;

      case VK_V:
        if (controlIsPressed(e)) {
          Lizzie.frame.pasteSgf();
        } else {
          Lizzie.config.toggleShowBranch();
        }
        break;

      case VK_HOME:
        if (controlIsPressed(e)) {
          Lizzie.board.clear();
        } else {
          while (Lizzie.board.previousMove()) ;
        }
        break;

      case VK_END:
        while (Lizzie.board.nextMove()) ;
        break;

      case VK_X:
        if (!Lizzie.frame.showControls) {
          if (Lizzie.leelaz.isPondering()) {
            wasPonderingWhenControlsShown = true;
            Lizzie.leelaz.togglePonder();
          } else {
            wasPonderingWhenControlsShown = false;
          }
          Lizzie.frame.drawControls();
        }
        Lizzie.frame.showControls = true;
        break;

      case VK_W:
        if (controlIsPressed(e)) {
          Lizzie.config.toggleLargeWinrate();
        } else if (e.isShiftDown()) {
          Lizzie.leelaz.set_opponent("black");
          Lizzie.leelaz.opponent_display = "WHITE ONLY";
        } else {
          Lizzie.config.toggleShowWinrate();
        }
        break;

      case VK_B:
        if (e.isShiftDown()) {
          Lizzie.leelaz.set_opponent("white");
          Lizzie.leelaz.opponent_display = "BLACK ONLY";
        }
        break;

      case VK_G:
        Lizzie.config.toggleShowVariationGraph();
        break;

      case VK_T:
        if (controlIsPressed(e)) {
          Lizzie.config.toggleShowCommentNodeColor();
        } else {
          Lizzie.config.toggleShowComment();
        }
        break;

      case VK_Y:
        Lizzie.config.toggleNodeColorMode();
        break;

      case VK_C:
        if (controlIsPressed(e)) {
          Lizzie.frame.copySgf();
        } else {
          Lizzie.config.toggleCoordinates();
        }
        break;

      case VK_ENTER:
        if (!Lizzie.leelaz.isThinking) {
          Lizzie.leelaz.sendCommand(
              "time_settings 0 "
                  + Lizzie.config
                      .config
                      .getJSONObject("leelaz")
                      .getInt("max-game-thinking-time-seconds")
                  + " 1");
          Lizzie.frame.playerIsBlack = !Lizzie.board.getData().blackToPlay;
          Lizzie.frame.isPlayingAgainstLeelaz = true;
          Lizzie.leelaz.genmove((Lizzie.board.getData().blackToPlay ? "B" : "W"));
        }
        break;

      case VK_DELETE:
      case VK_BACK_SPACE:
        if (e.isShiftDown()) {
          deleteBranch();
        } else {
          deleteMove();
        }
        break;

      case VK_Z:
        if (e.isShiftDown()) {
          toggleHints();
        } else {
          startRawBoard();
        }
        break;

      case VK_A:
        shouldDisableAnalysis = false;
        Lizzie.board.toggleAnalysis();
        break;

      case VK_PERIOD:
        if (!Lizzie.board.getHistory().getNext().isPresent()) {
          Lizzie.board.setScoreMode(!Lizzie.board.inScoreMode());
        }
        break;

      case VK_D:
        toggleShowDynamicKomi();
        break;

      case VK_OPEN_BRACKET:
        if (Lizzie.frame.BoardPositionProportion > 0) Lizzie.frame.BoardPositionProportion--;
        break;

      case VK_CLOSE_BRACKET:
        if (Lizzie.frame.BoardPositionProportion < 8) Lizzie.frame.BoardPositionProportion++;
        break;

        // Use Ctrl+Num to switching multiple engine
        // Use Num without CTRL to set search width
        // Push "1" for default LZ search, which is maximum narrowness.
        // Push "2" and beyond for increasingly wide search.
        // Pushing "0" is the maximum width search, temporarily set to roughly search across the
        // best 190 moves on the board.
        // shouldDisableAnalysis = false;
      case VK_BACK_QUOTE:
        shouldDisableAnalysis = false;
        if (!(controlIsPressed(e)) && !(e.isShiftDown())) {
          Lizzie.leelaz.set_search_width("0");
          Lizzie.leelaz.search_width_display = "DEFAULT LZ";
          break;
        }
        if (e.isShiftDown()) {
          Lizzie.leelaz.set_multidepth_search("0");
          break;
        }
        if (controlIsPressed(e)) {
          Lizzie.leelaz.reset_nncache();
          break;
        }
      case VK_0:
        shouldDisableAnalysis = false;
        if (!(controlIsPressed(e)) && !(e.isShiftDown())) {
          Lizzie.leelaz.set_search_width("10");
          Lizzie.leelaz.search_width_display = "10";
          break;
        }
        if (e.isShiftDown()) {
          // Lizzie.leelaz.set_multidepth_search("10");
          Lizzie.leelaz.set_komi_75();
          Lizzie.leelaz.search_width_display = "75";
          break;
        }
      case VK_1:
        shouldDisableAnalysis = false;
        if (!(controlIsPressed(e)) && !(e.isShiftDown())) {
          Lizzie.leelaz.set_search_width("1");
          Lizzie.leelaz.search_width_display = "1";
          break;
        }
        if (e.isShiftDown()) {
          Lizzie.leelaz.set_multidepth_search("1");
          break;
        }
      case VK_2:
        shouldDisableAnalysis = false;
        if (!(controlIsPressed(e)) && !(e.isShiftDown())) {
          Lizzie.leelaz.set_search_width("2");
          Lizzie.leelaz.search_width_display = "2";
          break;
        }
        if (e.isShiftDown()) {
          Lizzie.leelaz.set_multidepth_search("2");
          break;
        }
      case VK_3:
        shouldDisableAnalysis = false;
        if (!(controlIsPressed(e)) && !(e.isShiftDown())) {
          Lizzie.leelaz.set_search_width("3");
          Lizzie.leelaz.search_width_display = "3";
          break;
        }
        if (e.isShiftDown()) {
          Lizzie.leelaz.set_multidepth_search("3");
          break;
        }
      case VK_4:
        shouldDisableAnalysis = false;
        if (!(controlIsPressed(e)) && !(e.isShiftDown())) {
          Lizzie.leelaz.set_search_width("4");
          Lizzie.leelaz.search_width_display = "4";
          break;
        }
        if (e.isShiftDown()) {
          Lizzie.leelaz.set_multidepth_search("4");
          break;
        }
      case VK_5:
        shouldDisableAnalysis = false;
        if (!(controlIsPressed(e)) && !(e.isShiftDown())) {
          Lizzie.leelaz.set_search_width("5");
          Lizzie.leelaz.search_width_display = "5";
          break;
        }
        if (e.isShiftDown()) {
          Lizzie.leelaz.set_multidepth_search("5");
          break;
        }
      case VK_6:
        shouldDisableAnalysis = false;
        if (!(controlIsPressed(e)) && !(e.isShiftDown())) {
          Lizzie.leelaz.set_search_width("6");
          Lizzie.leelaz.search_width_display = "6";
          break;
        }
        if (e.isShiftDown()) {
          Lizzie.leelaz.set_multidepth_search("6");
          break;
        }
      case VK_7:
        shouldDisableAnalysis = false;
        if (!(controlIsPressed(e)) && !(e.isShiftDown())) {
          Lizzie.leelaz.set_search_width("7");
          Lizzie.leelaz.search_width_display = "7";
          break;
        }
        if (e.isShiftDown()) {
          Lizzie.leelaz.set_multidepth_search("7");
          break;
        }
      case VK_8:
        shouldDisableAnalysis = false;
        if (!(controlIsPressed(e)) && !(e.isShiftDown())) {
          Lizzie.leelaz.set_search_width("8");
          Lizzie.leelaz.search_width_display = "8";
          break;
        }
        if (e.isShiftDown()) {
          // Lizzie.leelaz.set_multidepth_search("8");
          Lizzie.leelaz.set_komi_05();
          Lizzie.leelaz.search_width_display = "05";
          break;
        }
      case VK_9:
        shouldDisableAnalysis = false;
        if (!(controlIsPressed(e)) && !(e.isShiftDown())) {
          Lizzie.leelaz.set_search_width("9");
          Lizzie.leelaz.search_width_display = "9";
          break;
        }
        if (e.isShiftDown()) {
          // Lizzie.leelaz.set_multidepth_search("9");
          Lizzie.leelaz.set_komi_65();
          Lizzie.leelaz.search_width_display = "65";
          break;
        }
        if (controlIsPressed(e)) {
          Lizzie.switchEngine(e.getKeyCode() - VK_0);
        }
        break;
      default:
        shouldDisableAnalysis = false;
    }

    if (shouldDisableAnalysis && Lizzie.board.inAnalysisMode()) Lizzie.board.toggleAnalysis();

    Lizzie.frame.repaint();
  }

  private boolean wasPonderingWhenControlsShown = false;

  @Override
  public void keyReleased(KeyEvent e) {
    switch (e.getKeyCode()) {
      case VK_X:
        if (wasPonderingWhenControlsShown) Lizzie.leelaz.togglePonder();
        Lizzie.frame.showControls = false;
        Lizzie.frame.repaint();
        break;

      case VK_Z:
        stopRawBoard();
        Lizzie.frame.repaint();
        break;

      default:
    }
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (Lizzie.frame.processCommentMouseWheelMoved(e)) {
      return;
    }
    if (Lizzie.board.inAnalysisMode()) Lizzie.board.toggleAnalysis();
    if (e.getWheelRotation() > 0) {
      redo();
    } else if (e.getWheelRotation() < 0) {
      undo();
    }
    Lizzie.frame.repaint();
  }
}
