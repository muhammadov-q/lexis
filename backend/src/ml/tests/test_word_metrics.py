import pytest

from ml.benchmark.word_metrics import (
    PageCounts,
    Word,
    boundary_counts,
    edit_distance,
    intervals,
    iou,
    match_boxes,
    merged_words,
    score_page,
    split_words,
    summarise,
)

LEFT = (0, 0, 10, 10)
RIGHT = (20, 0, 10, 10)
BOTH = (0, 0, 30, 10)


def test_iou_of_the_same_box_is_one():
    assert iou(LEFT, LEFT) == 1.0


def test_iou_of_boxes_apart_is_zero():
    assert iou(LEFT, RIGHT) == 0.0


def test_iou_is_shared_area_over_joint_area():
    assert iou((0, 0, 10, 10), (5, 0, 10, 10)) == pytest.approx(50 / 150)


def test_an_empty_box_has_no_overlap():
    assert iou((0, 0, 0, 0), (0, 0, 0, 0)) == 0.0


def test_each_box_is_matched_once_best_overlap_first():
    truth = [LEFT]
    predicted = [(1, 0, 10, 10), (0, 0, 10, 10)]
    assert match_boxes(truth, predicted) == [(0, 1, 1.0)]


def test_boxes_below_the_threshold_are_not_matched():
    assert match_boxes([LEFT], [(6, 0, 10, 10)]) == []


def test_a_box_over_two_words_merges_both():
    assert merged_words([LEFT, RIGHT], [BOTH]) == 2


def test_a_box_per_word_merges_nothing():
    assert merged_words([LEFT, RIGHT], [LEFT, RIGHT]) == 0


def test_a_word_holding_two_boxes_is_split():
    assert split_words([BOTH], [LEFT, RIGHT]) == 1


def test_a_box_per_word_splits_nothing():
    assert split_words([LEFT, RIGHT], [LEFT, RIGHT]) == 0


def test_the_same_words_share_every_boundary():
    words = ["καὶ", "ὑπὸ", "θεοῦ"]
    assert boundary_counts(words, words) == (3, 3, 3)


def test_a_merged_word_misses_a_boundary():
    truth = ["καὶ", "ὑπὸ", "θεοῦ", "τὴν"]
    predicted = ["καὶ", "ϋπὸθυ", "τὴν"]
    assert boundary_counts(truth, predicted) == (3, 3, 4)


def test_a_split_word_adds_a_boundary():
    assert boundary_counts(["τούτων"], ["του", "των"]) == (1, 2, 1)


def test_a_missed_accent_is_not_a_missed_boundary():
    assert boundary_counts(["τούτων", "καὶ"], ["τουτων", "και"]) == (2, 2, 2)


def test_missed_punctuation_is_not_a_missed_boundary():
    assert boundary_counts(["συγκείμενα,", "εἰ"], ["συγκειμενα", "εἰ"]) == (2, 2, 2)


def test_a_boundary_after_a_letter_only_one_side_read_is_left_out():
    assert boundary_counts(["αβγ", "δ"], ["αβ", "δ"]) == (1, 2, 1)


@pytest.mark.parametrize(
    ("a", "b", "distance"),
    [
        ("", "", 0),
        ("abc", "abc", 0),
        ("abc", "abd", 1),
        ("abc", "", 3),
        ("ab", "ba", 2),
    ],
)
def test_edit_distance(a, b, distance):
    assert edit_distance(a, b) == distance


def test_a_perfect_page_scores_full_marks():
    words = [Word("καὶ", LEFT), Word("τὴν", RIGHT)]
    summary = summarise([score_page(words, words)])
    assert summary["box_f1"] == 1.0
    assert summary["mean_iou"] == 1.0
    assert summary["boundary_f1"] == 1.0
    assert summary["exact_words"] == 1.0
    assert summary["merged_words"] == summary["split_words"] == summary["cer"] == 0.0


def test_a_merged_page_is_scored_as_merged():
    truth = [Word("ὑπὸ", LEFT), Word("θεοῦ", RIGHT)]
    page = score_page(truth, [Word("ϋπὸθυ", BOTH)])
    assert (page.truth_words, page.predicted_words, page.matched) == (2, 1, 0)
    assert (page.merged, page.split) == (2, 0)
    assert page.char_errors == 5
    assert page.truth_chars == len("ὑπὸ θεοῦ")


def test_a_right_box_with_the_wrong_text_is_not_exact():
    page = score_page([Word("καὶ", LEFT)], [Word("και", LEFT)])
    assert (page.matched, page.exact) == (1, 0)


def test_pages_are_summed_before_dividing():
    pages = [
        PageCounts(truth_words=10, predicted_words=10, matched=10),
        PageCounts(truth_words=30, predicted_words=10, matched=10),
    ]
    summary = summarise(pages)
    assert summary["box_recall"] == 0.5
    assert summary["box_precision"] == 1.0


def test_no_pages_gives_zeros():
    assert set(summarise([]).values()) == {0.0}
    assert set(intervals([]).values()) == {(0.0, 0.0)}


def test_intervals_hold_the_value_and_repeat():
    pages = [
        PageCounts(truth_words=10, predicted_words=10, matched=matched)
        for matched in (4, 6, 8, 10)
    ]
    low, high = intervals(pages)["box_recall"]
    assert low <= summarise(pages)["box_recall"] <= high
    assert low < high
    assert intervals(pages) == intervals(pages)


def test_one_page_has_no_spread():
    page = PageCounts(truth_words=10, predicted_words=10, matched=5)
    assert intervals([page])["box_recall"] == (0.5, 0.5)
